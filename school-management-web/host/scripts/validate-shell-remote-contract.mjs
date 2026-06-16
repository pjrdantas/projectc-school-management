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
const domainCatalog = readObjectMap(shellSource, 'SHELL_DOMAIN_CATALOG');
const exposes = readExposes(federationSource);

const errors = [
  ...validateDomainCatalog(domainCatalog, remoteRoutes, [...businessMenu, ...accessMenu]),
  ...validateRemoteRoutes(remoteRoutes, exposes),
  ...validateMenus([...businessMenu, ...accessMenu], remoteRoutes),
  ...validateMenuGroups('SHELL_BUSINESS_MENU_GROUPS', businessMenuGroups, businessMenu),
  ...validateMenuGroups('SHELL_ACCESS_MENU_GROUPS', accessMenuGroups, accessMenu),
];

if (errors.length > 0) {
  console.error('Contrato shell/microfrontend invalido:');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(
  `Contrato shell/microfrontend valido: ${remoteRoutes.length} rotas federadas, ${exposes.size} exposes e ${businessMenu.length + accessMenu.length} itens de menu conferidos.`,
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

  if (ts.isPropertyAccessExpression(node)) {
    return node.getText();
  }

  if (ts.isCallExpression(node)) {
    return node.getText();
  }

  if (ts.isArrayLiteralExpression(node)) {
    return node.elements.map(getLiteralValue);
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

  for (const route of routes) {
    requireString(route, 'path', `rota federada ${JSON.stringify(route)}`, errors);
    requireString(route, 'domain', `rota federada ${route.path}`, errors);
    requireString(route, 'exposedModule', `rota federada ${route.path}`, errors);
    requireString(route, 'exportName', `rota federada ${route.path}`, errors);

    if (routePaths.has(route.path)) {
      errors.push(`rota federada duplicada: ${route.path}`);
    }

    routePaths.add(route.path);
    routeExposes.add(route.exposedModule);

    if (!exposesSet.has(route.exposedModule)) {
      errors.push(`rota ${route.path} aponta para exposedModule inexistente: ${route.exposedModule}`);
    }
  }

  for (const exposedModule of exposesSet) {
    if (!routeExposes.has(exposedModule)) {
      errors.push(`exposedModule sem rota federada no shell: ${exposedModule}`);
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

function normalizeMenuRoute(route) {
  return route.replace(/^\/+/, '');
}

function isHostLocalRoute(routePath) {
  return routePath.startsWith('auth/');
}
