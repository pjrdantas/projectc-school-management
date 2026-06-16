import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import ts from 'typescript';

const hostRoot = process.cwd();
const workspaceRoot = path.resolve(hostRoot, '..');
const shellNavigationPath = path.join(
  hostRoot,
  'src',
  'app',
  'core',
  'shell',
  'shell-navigation.config.ts',
);
const remoteFederationPath = path.join(workspaceRoot, 'microfrontend', 'federation.config.js');

const shellSource = parseSource(shellNavigationPath, ts.ScriptKind.TS);
const federationSource = parseSource(remoteFederationPath, ts.ScriptKind.JS);

const remoteRoutes = readArrayObjects(shellSource, 'SHELL_REMOTE_ROUTES');
const businessMenu = readArrayObjects(shellSource, 'SHELL_BUSINESS_MENU');
const accessMenu = readArrayObjects(shellSource, 'SHELL_ACCESS_MENU');
const businessMenuGroups = readArrayObjects(shellSource, 'SHELL_BUSINESS_MENU_GROUPS');
const accessMenuGroups = readArrayObjects(shellSource, 'SHELL_ACCESS_MENU_GROUPS');
const extractionCandidates = readArrayObjects(shellSource, 'SHELL_EXTRACTION_CANDIDATES');
const domainCatalog = readObjectMap(shellSource, 'SHELL_DOMAIN_CATALOG');
const exposes = readExposes(federationSource);

const errors = [
  ...validateDomainCatalog(domainCatalog, remoteRoutes, [...businessMenu, ...accessMenu]),
  ...validateRemoteRoutes(remoteRoutes, exposes),
  ...validateMenus([...businessMenu, ...accessMenu], remoteRoutes),
  ...validateMenuGroups('SHELL_BUSINESS_MENU_GROUPS', businessMenuGroups, businessMenu),
  ...validateMenuGroups('SHELL_ACCESS_MENU_GROUPS', accessMenuGroups, accessMenu),
  ...validateExtractionCandidates(
    extractionCandidates,
    domainCatalog,
    remoteRoutes,
    [...businessMenu, ...accessMenu],
  ),
];

if (errors.length > 0) {
  console.error('Contrato shell/microfrontend invalido:');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(
  `Contrato shell/microfrontend valido: ${remoteRoutes.length} rotas federadas, ${exposes.size} exposes, ${businessMenu.length + accessMenu.length} itens de menu e ${extractionCandidates.length} candidatos de extracao conferidos.`,
);

function parseSource(filePath, scriptKind) {
  const sourceText = fs.readFileSync(filePath, 'utf8');
  return ts.createSourceFile(filePath, sourceText, ts.ScriptTarget.Latest, true, scriptKind);
}

function readArrayObjects(sourceFile, variableName) {
  const declaration = findVariableDeclaration(sourceFile, variableName);
  const initializer = unwrapExpression(declaration?.initializer);

  if (!declaration || !ts.isArrayLiteralExpression(initializer)) {
    throw new Error(`Nao foi possivel localizar o array ${variableName}.`);
  }

  return initializer.elements.map((element) => {
    if (!ts.isObjectLiteralExpression(element)) {
      throw new Error(`${variableName} deve conter apenas objetos literais.`);
    }

    return readObjectLiteral(element);
  });
}

function readObjectMap(sourceFile, variableName) {
  const declaration = findVariableDeclaration(sourceFile, variableName);
  const initializer = unwrapExpression(declaration?.initializer);

  if (!declaration || !ts.isObjectLiteralExpression(initializer)) {
    throw new Error(`Nao foi possivel localizar o objeto ${variableName}.`);
  }

  const result = new Map();

  for (const property of initializer.properties) {
    if (!ts.isPropertyAssignment(property) || !ts.isObjectLiteralExpression(property.initializer)) {
      throw new Error(`${variableName} deve conter apenas objetos literais.`);
    }

    result.set(getPropertyName(property.name), readObjectLiteral(property.initializer));
  }

  return result;
}

function findVariableDeclaration(sourceFile, variableName) {
  let result;

  visit(sourceFile);
  return result;

  function visit(node) {
    if (result) return;

    if (ts.isVariableDeclaration(node) && node.name.getText(sourceFile) === variableName) {
      result = node;
      return;
    }

    ts.forEachChild(node, visit);
  }
}

function unwrapExpression(node) {
  if (!node) return node;

  if (ts.isAsExpression(node) || ts.isSatisfiesExpression(node)) {
    return unwrapExpression(node.expression);
  }

  return node;
}

function readObjectLiteral(objectLiteral) {
  const result = {};

  for (const property of objectLiteral.properties) {
    if (!ts.isPropertyAssignment(property)) continue;

    const key = getPropertyName(property.name);
    const value = getLiteralValue(property.initializer);

    result[key] = value;
  }

  return result;
}

function readExposes(sourceFile) {
  let exposesObject;

  visit(sourceFile);

  if (!exposesObject) {
    throw new Error('Nao foi possivel localizar a propriedade exposes no federation.config.js.');
  }

  return new Set(
    exposesObject.properties
      .filter(ts.isPropertyAssignment)
      .map((property) => getPropertyName(property.name)),
  );

  function visit(node) {
    if (exposesObject) return;

    if (
      ts.isPropertyAssignment(node) &&
      getPropertyName(node.name) === 'exposes' &&
      ts.isObjectLiteralExpression(node.initializer)
    ) {
      exposesObject = node.initializer;
      return;
    }

    ts.forEachChild(node, visit);
  }
}

function getPropertyName(name) {
  if (ts.isIdentifier(name) || ts.isStringLiteral(name) || ts.isNumericLiteral(name)) {
    return name.text;
  }

  throw new Error(`Nome de propriedade nao suportado: ${name.getText()}`);
}

function getLiteralValue(node) {
  if (ts.isStringLiteral(node) || ts.isNoSubstitutionTemplateLiteral(node)) {
    return node.text;
  }

  if (node.kind === ts.SyntaxKind.TrueKeyword) {
    return true;
  }

  if (node.kind === ts.SyntaxKind.FalseKeyword) {
    return false;
  }

  if (ts.isPropertyAccessExpression(node)) {
    return node.getText();
  }

  if (ts.isCallExpression(node)) {
    return node.getText();
  }

  if (ts.isArrayLiteralExpression(node)) {
    return node.elements.map(getLiteralValue);
  }

  if (ts.isObjectLiteralExpression(node)) {
    return readObjectLiteral(node);
  }

  return node.getText();
}

function validateDomainCatalog(domainCatalog, routes, menuItems) {
  const errors = [];

  for (const [key, item] of domainCatalog.entries()) {
    requireString(item, 'domain', `dominio catalogado ${key}`, errors);
    requireString(item, 'label', `dominio catalogado ${key}`, errors);
    requireString(item, 'futureRemoteName', `dominio catalogado ${key}`, errors);
    requireString(item, 'currentPlacement', `dominio catalogado ${key}`, errors);

    if (item.domain !== key) {
      errors.push(`dominio catalogado ${key} declara domain diferente: ${item.domain}`);
    }
  }

  for (const route of routes) {
    if (!domainCatalog.has(route.domain)) {
      errors.push(`rota ${route.path} usa dominio nao catalogado: ${route.domain}`);
    }
  }

  for (const item of menuItems) {
    if (!domainCatalog.has(item.domain)) {
      errors.push(`item de menu ${item.label} usa dominio nao catalogado: ${item.domain}`);
    }
  }

  return errors;
}

function validateMenuGroups(groupName, groups, expectedMenuItems) {
  const errors = [];
  const groupedRoutes = [];

  for (const group of groups) {
    requireString(group, 'domain', `${groupName} ${JSON.stringify(group)}`, errors);
    requireString(group, 'label', `${groupName} ${group.domain}`, errors);

    if (typeof group.items !== 'string' || !group.items.includes(`item.domain === '${group.domain}'`)) {
      errors.push(`${groupName} do dominio ${group.domain} deve filtrar itens pelo proprio dominio.`);
    }

    groupedRoutes.push(
      ...expectedMenuItems.filter(item => item.domain === group.domain).map(item => item.route),
    );
  }

  const expectedRoutes = expectedMenuItems.map(item => item.route);

  if (groupedRoutes.length !== expectedRoutes.length) {
    errors.push(`${groupName} nao cobre a mesma quantidade de itens do menu plano.`);
  }

  for (const [index, route] of expectedRoutes.entries()) {
    if (groupedRoutes[index] !== route) {
      errors.push(
        `${groupName} altera a ordem do menu plano na posicao ${index + 1}: esperado ${route}, encontrado ${groupedRoutes[index] ?? 'vazio'}`,
      );
    }
  }

  return errors;
}

function validateRemoteRoutes(routes, exposesSet) {
  const errors = [];
  const routeExposes = new Set();
  const routePaths = new Set();
  const dashboardCandidateRoutes = new Set([
    'dashboard',
    'dashboard/config',
    'dashboard/snapshots',
  ]);

  for (const route of routes) {
    requireString(route, 'path', `rota federada ${JSON.stringify(route)}`, errors);
    requireString(route, 'domain', `rota federada ${route.path}`, errors);
    requireString(route, 'runtimeRemoteName', `rota federada ${route.path}`, errors);
    requireString(route, 'exposedModule', `rota federada ${route.path}`, errors);
    requireString(route, 'exportName', `rota federada ${route.path}`, errors);

    if (routePaths.has(route.path)) {
      errors.push(`rota federada duplicada: ${route.path}`);
    }

    routePaths.add(route.path);
    routeExposes.add(route.exposedModule);

    if (route.runtimeRemoteName !== 'mfe1') {
      errors.push(
        `rota ${route.path} deve continuar carregando o remote atual mfe1 nesta fase, encontrado ${route.runtimeRemoteName}`,
      );
    }

    if (!exposesSet.has(route.exposedModule)) {
      errors.push(`rota ${route.path} aponta para exposedModule inexistente: ${route.exposedModule}`);
    }

    validateExtractionPlan(route, dashboardCandidateRoutes, errors);
  }

  for (const exposedModule of exposesSet) {
    if (!routeExposes.has(exposedModule)) {
      errors.push(`exposedModule sem rota federada no shell: ${exposedModule}`);
    }
  }

  return errors;
}

function validateExtractionPlan(route, dashboardCandidateRoutes, errors) {
  const extractionPlan = route.extractionPlan;
  const shouldBeDashboardCandidate = dashboardCandidateRoutes.has(route.path);

  if (!shouldBeDashboardCandidate) {
    if (extractionPlan !== undefined) {
      errors.push(`rota ${route.path} nao deve declarar extractionPlan nesta fase.`);
    }

    return;
  }

  if (!isRecord(extractionPlan)) {
    errors.push(`rota ${route.path} deve declarar extractionPlan como objeto literal.`);
    return;
  }

  requireBoolean(extractionPlan, 'candidate', `extractionPlan da rota ${route.path}`, errors);
  requireString(extractionPlan, 'targetRemoteName', `extractionPlan da rota ${route.path}`, errors);
  requireString(extractionPlan, 'routeRole', `extractionPlan da rota ${route.path}`, errors);
  requireString(extractionPlan, 'shellNavigation', `extractionPlan da rota ${route.path}`, errors);

  if (route.domain !== 'dashboard') {
    errors.push(`rota ${route.path} candidata a extracao deve pertencer ao dominio dashboard.`);
  }

  if (extractionPlan.candidate !== true) {
    errors.push(`rota ${route.path} deve marcar extractionPlan.candidate como true.`);
  }

  if (extractionPlan.targetRemoteName !== 'mfe-dashboard') {
    errors.push(
      `rota ${route.path} deve apontar extractionPlan.targetRemoteName para mfe-dashboard.`,
    );
  }

  if (route.path === 'dashboard') {
    if (extractionPlan.routeRole !== 'operational') {
      errors.push(`rota ${route.path} deve marcar extractionPlan.routeRole como operational.`);
    }

    if (extractionPlan.shellNavigation !== 'landing') {
      errors.push(`rota ${route.path} deve marcar extractionPlan.shellNavigation como landing.`);
    }
  }

  if (route.path === 'dashboard/config' || route.path === 'dashboard/snapshots') {
    if (extractionPlan.routeRole !== 'administrative') {
      errors.push(`rota ${route.path} deve marcar extractionPlan.routeRole como administrative.`);
    }

    if (extractionPlan.shellNavigation !== 'access-menu') {
      errors.push(`rota ${route.path} deve marcar extractionPlan.shellNavigation como access-menu.`);
    }
  }
}

function validateExtractionCandidates(candidates, domainCatalog, routes, menuItems) {
  const errors = [];
  const seenDomains = new Set();
  const candidateRoutes = routes.filter(route => route.extractionPlan?.candidate === true);
  const candidateDomains = new Set(candidateRoutes.map(route => route.domain));
  const candidateRoutesByDomain = groupBy(candidateRoutes, route => route.domain);

  for (const candidate of candidates) {
    requireString(candidate, 'domain', `manifesto de extracao ${JSON.stringify(candidate)}`, errors);
    requireString(candidate, 'label', `manifesto de extracao ${candidate.domain}`, errors);
    requireString(
      candidate,
      'currentPlacement',
      `manifesto de extracao ${candidate.domain}`,
      errors,
    );
    requireString(
      candidate,
      'targetRemoteName',
      `manifesto de extracao ${candidate.domain}`,
      errors,
    );
    requireStringArray(
      candidate,
      'runtimeRemoteNames',
      `manifesto de extracao ${candidate.domain}`,
      errors,
    );
    requireStringArray(
      candidate,
      'expectedExposedModules',
      `manifesto de extracao ${candidate.domain}`,
      errors,
    );
    requireStringArray(candidate, 'routePaths', `manifesto de extracao ${candidate.domain}`, errors);
    requireStringArray(
      candidate,
      'operationalRoutePaths',
      `manifesto de extracao ${candidate.domain}`,
      errors,
    );
    requireStringArray(
      candidate,
      'administrativeRoutePaths',
      `manifesto de extracao ${candidate.domain}`,
      errors,
    );
    requireStringArray(
      candidate,
      'landingRoutes',
      `manifesto de extracao ${candidate.domain}`,
      errors,
    );
    requireStringArray(
      candidate,
      'businessMenuRoutes',
      `manifesto de extracao ${candidate.domain}`,
      errors,
    );
    requireStringArray(
      candidate,
      'accessMenuRoutes',
      `manifesto de extracao ${candidate.domain}`,
      errors,
    );

    if (seenDomains.has(candidate.domain)) {
      errors.push(`manifesto de extracao duplicado para o dominio ${candidate.domain}.`);
      continue;
    }

    seenDomains.add(candidate.domain);

    const catalogItem = domainCatalog.get(candidate.domain);

    if (!catalogItem) {
      errors.push(`manifesto de extracao usa dominio nao catalogado: ${candidate.domain}`);
      continue;
    }

    if (!candidateDomains.has(candidate.domain)) {
      errors.push(
        `manifesto de extracao do dominio ${candidate.domain} nao possui rotas com extractionPlan candidato.`,
      );
      continue;
    }

    if (candidate.label !== catalogItem.label) {
      errors.push(
        `manifesto de extracao do dominio ${candidate.domain} deve usar o label ${catalogItem.label}.`,
      );
    }

    if (candidate.currentPlacement !== catalogItem.currentPlacement) {
      errors.push(
        `manifesto de extracao do dominio ${candidate.domain} deve usar currentPlacement ${catalogItem.currentPlacement}.`,
      );
    }

    if (candidate.targetRemoteName !== catalogItem.futureRemoteName) {
      errors.push(
        `manifesto de extracao do dominio ${candidate.domain} deve apontar targetRemoteName para ${catalogItem.futureRemoteName}.`,
      );
    }

    const domainRoutes = candidateRoutesByDomain.get(candidate.domain) ?? [];
    const expectedExposedModules = domainRoutes.map(route => route.exposedModule);
    const expectedRoutePaths = domainRoutes.map(route => route.path);
    const expectedRuntimeRemoteNames = [...new Set(domainRoutes.map(route => route.runtimeRemoteName))];
    const expectedOperationalRoutePaths = domainRoutes
      .filter(route => route.extractionPlan?.routeRole === 'operational')
      .map(route => route.path);
    const expectedAdministrativeRoutePaths = domainRoutes
      .filter(route => route.extractionPlan?.routeRole === 'administrative')
      .map(route => route.path);
    const expectedLandingRoutes = domainRoutes
      .filter(route => route.extractionPlan?.shellNavigation === 'landing')
      .map(route => route.path);
    const expectedBusinessMenuRoutes = menuItems
      .filter(item => item.domain === candidate.domain)
      .map(item => item.route)
      .filter(route => {
        const remoteRoute = domainRoutes.find(item => normalizeMenuRoute(route) === item.path);
        return remoteRoute?.extractionPlan?.shellNavigation === 'business-menu';
      });
    const expectedAccessMenuRoutes = menuItems
      .filter(item => item.domain === candidate.domain)
      .map(item => item.route)
      .filter(route => {
        const remoteRoute = domainRoutes.find(item => normalizeMenuRoute(route) === item.path);
        return remoteRoute?.extractionPlan?.shellNavigation === 'access-menu';
      });

    validateOrderedArray(
      candidate.expectedExposedModules,
      expectedExposedModules,
      `manifesto de extracao do dominio ${candidate.domain}`,
      'expectedExposedModules',
      errors,
    );
    validateOrderedArray(
      candidate.routePaths,
      expectedRoutePaths,
      `manifesto de extracao do dominio ${candidate.domain}`,
      'routePaths',
      errors,
    );
    validateOrderedArray(
      candidate.runtimeRemoteNames,
      expectedRuntimeRemoteNames,
      `manifesto de extracao do dominio ${candidate.domain}`,
      'runtimeRemoteNames',
      errors,
    );
    validateOrderedArray(
      candidate.operationalRoutePaths,
      expectedOperationalRoutePaths,
      `manifesto de extracao do dominio ${candidate.domain}`,
      'operationalRoutePaths',
      errors,
    );
    validateOrderedArray(
      candidate.administrativeRoutePaths,
      expectedAdministrativeRoutePaths,
      `manifesto de extracao do dominio ${candidate.domain}`,
      'administrativeRoutePaths',
      errors,
    );
    validateOrderedArray(
      candidate.landingRoutes,
      expectedLandingRoutes,
      `manifesto de extracao do dominio ${candidate.domain}`,
      'landingRoutes',
      errors,
    );
    validateOrderedArray(
      candidate.businessMenuRoutes,
      expectedBusinessMenuRoutes,
      `manifesto de extracao do dominio ${candidate.domain}`,
      'businessMenuRoutes',
      errors,
    );
    validateOrderedArray(
      candidate.accessMenuRoutes,
      expectedAccessMenuRoutes,
      `manifesto de extracao do dominio ${candidate.domain}`,
      'accessMenuRoutes',
      errors,
    );
  }

  for (const candidateDomain of candidateDomains) {
    if (!seenDomains.has(candidateDomain)) {
      errors.push(
        `dominio ${candidateDomain} possui rotas candidatas a extracao, mas nao esta no manifesto de extracao.`,
      );
    }
  }

  return errors;
}

function validateMenus(menuItems, routes) {
  const errors = [];
  const routesByPath = new Map(routes.map((route) => [route.path, route]));

  for (const item of menuItems) {
    requireString(item, 'label', `item de menu ${JSON.stringify(item)}`, errors);
    requireString(item, 'route', `item de menu ${item.label}`, errors);
    requireString(item, 'domain', `item de menu ${item.label}`, errors);

    const routePath = normalizeMenuRoute(item.route);
    const remoteRoute = routesByPath.get(routePath);

    if (!remoteRoute && !isHostLocalRoute(routePath)) {
      errors.push(`item de menu ${item.label} aponta para rota sem contrato: ${item.route}`);
      continue;
    }

    if (remoteRoute && remoteRoute.domain !== item.domain) {
      errors.push(
        `item de menu ${item.label} usa dominio ${item.domain}, mas a rota ${item.route} usa ${remoteRoute.domain}`,
      );
    }
  }

  return errors;
}

function requireString(record, property, context, errors) {
  if (typeof record[property] !== 'string' || record[property].trim() === '') {
    errors.push(`${context} deve informar ${property}.`);
  }
}

function requireBoolean(record, property, context, errors) {
  if (typeof record[property] !== 'boolean') {
    errors.push(`${context} deve informar ${property} booleano.`);
  }
}

function requireStringArray(record, property, context, errors) {
  if (
    !Array.isArray(record[property]) ||
    record[property].some(item => typeof item !== 'string' || item.trim() === '')
  ) {
    errors.push(`${context} deve informar ${property} com array de strings.`);
  }
}

function isRecord(value) {
  return typeof value === 'object' && value !== null;
}

function groupBy(items, keySelector) {
  const result = new Map();

  for (const item of items) {
    const key = keySelector(item);
    const group = result.get(key);

    if (group) {
      group.push(item);
      continue;
    }

    result.set(key, [item]);
  }

  return result;
}

function validateOrderedArray(actual, expected, context, property, errors) {
  if (actual.length !== expected.length) {
    errors.push(
      `${context} deve manter ${property} com ${expected.length} item(ns); encontrado ${actual.length}.`,
    );
    return;
  }

  for (const [index, expectedValue] of expected.entries()) {
    if (actual[index] !== expectedValue) {
      errors.push(
        `${context} altera ${property} na posicao ${index + 1}: esperado ${expectedValue}, encontrado ${actual[index] ?? 'vazio'}.`,
      );
    }
  }
}

function normalizeMenuRoute(route) {
  return route.replace(/^\/+/, '');
}

function isHostLocalRoute(routePath) {
  return routePath.startsWith('auth/');
}
