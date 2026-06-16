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
const exposes = readExposes(federationSource);

const errors = [
  ...validateRemoteRoutes(remoteRoutes, exposes),
  ...validateMenus([...businessMenu, ...accessMenu], remoteRoutes),
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
  if (!declaration || !ts.isArrayLiteralExpression(declaration.initializer)) {
    throw new Error(`Nao foi possivel localizar o array ${variableName}.`);
  }

  return declaration.initializer.elements.map((element) => {
    if (!ts.isObjectLiteralExpression(element)) {
      throw new Error(`${variableName} deve conter apenas objetos literais.`);
    }

    return readObjectLiteral(element);
  });
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

  if (ts.isArrayLiteralExpression(node)) {
    return node.elements.map(getLiteralValue);
  }

  return node.getText();
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
