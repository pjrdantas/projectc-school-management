import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import ts from 'typescript';

const hostRoot = process.cwd();
const shellNavigationPath = path.join(
  hostRoot,
  'src',
  'app',
  'core',
  'shell',
  'shell-navigation.config.ts',
);
const activeManifestPath = path.join(hostRoot, 'public', 'federation.manifest.json');
const standbyManifestPath = path.join(hostRoot, 'public', 'federation.standby.manifest.json');
const routeCutoversPath = path.join(hostRoot, 'public', 'shell-route-cutovers.json');

const shellSource = parseSource(shellNavigationPath, ts.ScriptKind.TS);
const remoteRoutes = readArrayObjects(shellSource, 'SHELL_REMOTE_ROUTES');
const activeManifest = readJson(activeManifestPath);
const standbyManifest = readJson(standbyManifestPath);
const routeCutovers = readJson(routeCutoversPath);

const errors = [
  ...validateRouteCutoverShape(routeCutovers),
  ...validateRouteCutovers(routeCutovers, remoteRoutes, activeManifest, standbyManifest),
];

if (errors.length > 0) {
  console.error('Configuracao de cutover por rota do shell invalida:');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(
  `Configuracao de cutover por rota valida: ${Object.keys(routeCutovers).length} rota(s) conferida(s) contra shell e manifestos de remotes.`,
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

  return initializer.elements.map(element => {
    const resolvedElement = unwrapExpression(element);

    if (!ts.isObjectLiteralExpression(resolvedElement)) {
      throw new Error(`${variableName} deve conter apenas objetos literais.`);
    }

    return readObjectLiteral(resolvedElement);
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
    result[key] = getLiteralValue(property.initializer);
  }

  return result;
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

  if (ts.isArrayLiteralExpression(node)) {
    return node.elements.map(getLiteralValue);
  }

  if (ts.isObjectLiteralExpression(node)) {
    return readObjectLiteral(node);
  }

  return node.getText();
}

function readJson(filePath) {
  return JSON.parse(fs.readFileSync(filePath, 'utf8'));
}

function validateRouteCutoverShape(routeCutovers) {
  const errors = [];

  if (!routeCutovers || typeof routeCutovers !== 'object' || Array.isArray(routeCutovers)) {
    errors.push('shell-route-cutovers.json deve conter um objeto simples.');
    return errors;
  }

  for (const [routePath, remoteName] of Object.entries(routeCutovers)) {
    if (!routePath.trim()) {
      errors.push('shell-route-cutovers.json nao pode declarar chave de rota vazia.');
    }

    if (typeof remoteName !== 'string' || !remoteName.trim()) {
      errors.push(`rota ${routePath} deve informar um remoteName nao vazio.`);
    }
  }

  return errors;
}

function validateRouteCutovers(routeCutovers, remoteRoutes, activeManifest, standbyManifest) {
  const errors = [];
  const routesByPath = new Map(remoteRoutes.map(route => [route.path, route]));

  for (const [routePath, remoteName] of Object.entries(routeCutovers)) {
    const route = routesByPath.get(routePath);

    if (!route) {
      errors.push(`rota ${routePath} nao existe em SHELL_REMOTE_ROUTES.`);
      continue;
    }

    const extractionPlan = route.extractionPlan;
    const allowedRemoteNames = new Set([route.runtimeRemoteName]);

    if (extractionPlan?.candidate && extractionPlan.targetRemoteName) {
      allowedRemoteNames.add(extractionPlan.targetRemoteName);
    }

    if (!allowedRemoteNames.has(remoteName)) {
      errors.push(
        `rota ${routePath} so pode alternar entre ${[...allowedRemoteNames].join(' e ')}, encontrado ${remoteName}.`,
      );
    }

    if (activeManifest[remoteName]) {
      continue;
    }

    if (!standbyManifest[remoteName]) {
      errors.push(
        `remote ${remoteName} configurado para a rota ${routePath} nao existe em federation.manifest.json nem em federation.standby.manifest.json.`,
      );
    }
  }

  return errors;
}
