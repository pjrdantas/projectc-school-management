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
const standbyManifestPath = path.join(hostRoot, 'public', 'federation.standby.manifest.json');

const shellSource = parseSource(shellNavigationPath, ts.ScriptKind.TS);
const extractionCandidates = readArrayObjects(shellSource, 'SHELL_EXTRACTION_CANDIDATES');
const standbyManifest = readStandbyManifest(standbyManifestPath);

const errors = [
  ...validateStandbyManifestShape(standbyManifest),
  ...validateStandbyRemotes(standbyManifest, extractionCandidates),
];

if (errors.length > 0) {
  console.error('Manifesto de remotes futuros do shell invalido:');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(
  `Manifesto de remotes futuros valido: ${Object.keys(standbyManifest).length} remote(s) em espera conferido(s) contra shell, angular.json e federation.config.js.`,
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

function readStandbyManifest(filePath) {
  return JSON.parse(fs.readFileSync(filePath, 'utf8'));
}

function validateStandbyManifestShape(standbyManifest) {
  const errors = [];

  if (!standbyManifest || typeof standbyManifest !== 'object' || Array.isArray(standbyManifest)) {
    errors.push('federation.standby.manifest.json deve conter um objeto simples.');
    return errors;
  }

  for (const [remoteName, remoteEntryUrl] of Object.entries(standbyManifest)) {
    if (!remoteName.startsWith('mfe-')) {
      errors.push(`remote em espera deve usar prefixo mfe-: ${remoteName}.`);
    }

    if (typeof remoteEntryUrl !== 'string' || remoteEntryUrl.trim() === '') {
      errors.push(`remote ${remoteName} deve informar a URL do remoteEntry.`);
      continue;
    }

    let parsedUrl;
    try {
      parsedUrl = new URL(remoteEntryUrl);
    } catch {
      errors.push(`remote ${remoteName} usa URL invalida: ${remoteEntryUrl}.`);
      continue;
    }

    if (parsedUrl.pathname !== '/remoteEntry.json') {
      errors.push(
        `remote ${remoteName} deve apontar para /remoteEntry.json, encontrado ${parsedUrl.pathname}.`,
      );
    }
  }

  return errors;
}

function validateStandbyRemotes(standbyManifest, extractionCandidates) {
  const errors = [];

  for (const [remoteName, remoteEntryUrl] of Object.entries(standbyManifest)) {
    const candidates = extractionCandidates.filter(
      candidate => candidate.targetRemoteName === remoteName,
    );

    if (candidates.length === 0) {
      errors.push(
        `remote em espera ${remoteName} nao possui candidato correspondente em SHELL_EXTRACTION_CANDIDATES.`,
      );
      continue;
    }

    const remoteRoot = path.join(workspaceRoot, remoteName);
    const federationPath = path.join(remoteRoot, 'federation.config.js');
    const angularPath = path.join(remoteRoot, 'angular.json');

    if (!fs.existsSync(federationPath)) {
      errors.push(`remote em espera ${remoteName} nao possui federation.config.js em ${remoteRoot}.`);
      continue;
    }

    if (!fs.existsSync(angularPath)) {
      errors.push(`remote em espera ${remoteName} nao possui angular.json em ${remoteRoot}.`);
      continue;
    }

    const federationSource = parseSource(federationPath, ts.ScriptKind.JS);
    const federationName = readNamedProperty(federationSource, 'name');
    const federationExposes = readExposes(federationSource);
    const angularJson = JSON.parse(fs.readFileSync(angularPath, 'utf8'));
    const configuredPort = readServePort(angularJson, remoteName);
    const expectedPort = new URL(remoteEntryUrl).port;
    const expectedExposedModules = [...new Set(candidates.flatMap(candidate => candidate.expectedExposedModules ?? []))];

    if (federationName !== remoteName) {
      errors.push(
        `remote ${remoteName} deve declarar name=${remoteName} em federation.config.js, encontrado ${federationName}.`,
      );
    }

    if (String(configuredPort) !== expectedPort) {
      errors.push(
        `remote ${remoteName} deve alinhar a porta ${configuredPort} do angular.json com ${expectedPort} do manifesto em espera.`,
      );
    }

    for (const exposedModule of expectedExposedModules) {
      if (!federationExposes.has(exposedModule)) {
        errors.push(
          `remote ${remoteName} nao expoe ${exposedModule}, esperado pelo shell para cutover futuro.`,
        );
      }
    }
  }

  return errors;
}

function readNamedProperty(sourceFile, propertyName) {
  let result = null;

  visit(sourceFile);
  return result;

  function visit(node) {
    if (result !== null) return;

    if (
      ts.isPropertyAssignment(node) &&
      getPropertyName(node.name) === propertyName &&
      (ts.isStringLiteral(node.initializer) || ts.isNoSubstitutionTemplateLiteral(node.initializer))
    ) {
      result = node.initializer.text;
      return;
    }

    ts.forEachChild(node, visit);
  }
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
      .map(property => getPropertyName(property.name)),
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

function readServePort(angularJson, remoteName) {
  const project = angularJson?.projects?.[remoteName];
  const port = project?.architect?.['serve-original']?.options?.port;

  if (typeof port !== 'number') {
    throw new Error(
      `Nao foi possivel localizar serve-original.options.port em angular.json para ${remoteName}.`,
    );
  }

  return port;
}
