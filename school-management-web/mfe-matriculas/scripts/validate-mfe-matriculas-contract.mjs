import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import ts from 'typescript';

const remoteRoot = process.cwd();
const manifestPath = path.join(
  remoteRoot,
  'src',
  'app',
  'matricula',
  'enrollment-domain.manifest.ts',
);
const federationPath = path.join(remoteRoot, 'federation.config.js');
const shellNavigationPath = path.join(
  remoteRoot,
  '..',
  'host',
  'src',
  'app',
  'core',
  'shell',
  'shell-navigation.config.ts',
);

const manifestSource = parseSource(manifestPath, ts.ScriptKind.TS);
const federationSource = parseSource(federationPath, ts.ScriptKind.JS);
const shellSource = parseSource(shellNavigationPath, ts.ScriptKind.TS);

const manifestItems = readArrayObjects(manifestSource, 'ENROLLMENT_DOMAIN_MANIFEST');
const exposes = readExposes(federationSource);
const shellEnrollmentRoutes = readArrayObjects(shellSource, 'SHELL_REMOTE_ROUTES').filter(
  route => route.domain === 'matriculas',
);
const shellEnrollmentCandidates = readArrayObjects(
  shellSource,
  'SHELL_EXTRACTION_CANDIDATES',
).filter(candidate => candidate.domain === 'matriculas');

const errors = [
  ...validateManifestShape(manifestItems),
  ...validateFederation(manifestItems, exposes),
  ...validateExposeFiles(manifestItems),
  ...validateShellContract(
    manifestItems,
    shellEnrollmentRoutes,
    shellEnrollmentCandidates,
  ),
];

if (errors.length > 0) {
  console.error('Contrato do mfe-matriculas invalido:');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(
  `Contrato do mfe-matriculas valido: ${manifestItems.length} entrada conferida entre manifesto, expose local, federation.config.js e shell do host.`,
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

function readExposes(sourceFile) {
  let exposesObject;

  visit(sourceFile);

  if (!exposesObject) {
    throw new Error('Nao foi possivel localizar a propriedade exposes no federation.config.js.');
  }

  return new Map(
    exposesObject.properties
      .filter(ts.isPropertyAssignment)
      .map(property => [getPropertyName(property.name), getLiteralValue(property.initializer)]),
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

  if (ts.isArrayLiteralExpression(node)) {
    return node.elements.map(getLiteralValue);
  }

  if (ts.isObjectLiteralExpression(node)) {
    return readObjectLiteral(node);
  }

  return node.getText();
}

function validateManifestShape(manifestItems) {
  const errors = [];
  const [item] = manifestItems;

  if (manifestItems.length !== 1) {
    errors.push(`manifesto matriculas deve conter exatamente 1 rota, encontradas ${manifestItems.length}.`);
    return errors;
  }

  requireString(item, 'key', 'item do manifesto matriculas', errors);
  requireString(item, 'domain', 'item do manifesto matriculas', errors);
  requireString(item, 'futureRemoteName', 'item do manifesto matriculas', errors);
  requireString(item, 'path', 'item do manifesto matriculas', errors);
  requireString(item, 'exposedModule', 'item do manifesto matriculas', errors);
  requireString(item, 'exposeFilePath', 'item do manifesto matriculas', errors);
  requireString(item, 'exportName', 'item do manifesto matriculas', errors);
  requireString(item, 'routeKind', 'item do manifesto matriculas', errors);
  requireString(item, 'routeRole', 'item do manifesto matriculas', errors);
  requireString(item, 'shellNavigation', 'item do manifesto matriculas', errors);
  requireBoolean(item, 'extractionCandidate', 'item do manifesto matriculas', errors);

  if (item.domain !== 'matriculas') {
    errors.push(`manifesto matriculas deve usar o dominio matriculas, encontrado ${item.domain}.`);
  }

  if (item.futureRemoteName !== 'mfe-matriculas') {
    errors.push(
      `manifesto matriculas deve usar futureRemoteName mfe-matriculas, encontrado ${item.futureRemoteName}.`,
    );
  }

  if (item.path !== 'enrollment') {
    errors.push(`manifesto matriculas deve usar a rota enrollment, encontrado ${item.path}.`);
  }

  if (item.routeKind !== 'create') {
    errors.push(`rota ${item.path} deve marcar routeKind como create.`);
  }

  if (item.routeRole !== 'operational') {
    errors.push(`rota ${item.path} deve marcar routeRole como operational.`);
  }

  if (item.shellNavigation !== 'business-menu') {
    errors.push(`rota ${item.path} deve marcar shellNavigation como business-menu.`);
  }

  if (item.extractionCandidate !== false) {
    errors.push(`rota ${item.path} deve marcar extractionCandidate=false no remote dedicado.`);
  }

  return errors;
}

function validateFederation(manifestItems, exposes) {
  const errors = [];
  const [item] = manifestItems;
  const federationExposePath = exposes.get(item.exposedModule);

  if (!federationExposePath) {
    errors.push(`federation.config.js nao expoe o modulo ${item.exposedModule}.`);
    return errors;
  }

  if (federationExposePath !== item.exposeFilePath) {
    errors.push(
      `modulo ${item.exposedModule} deve apontar para ${item.exposeFilePath} em federation.config.js, encontrado ${federationExposePath}.`,
    );
  }

  return errors;
}

function validateExposeFiles(manifestItems) {
  const errors = [];
  const [item] = manifestItems;
  const exposeFilePath = path.join(remoteRoot, toSystemPath(item.exposeFilePath));

  if (!fs.existsSync(exposeFilePath)) {
    errors.push(`arquivo de expose ausente para ${item.exposedModule}: ${item.exposeFilePath}.`);
    return errors;
  }

  const exposeSource = parseSource(exposeFilePath, ts.ScriptKind.TS);
  const exportDeclaration = readExposeExport(exposeSource);

  if (!exportDeclaration) {
    errors.push(`arquivo ${item.exposeFilePath} deve reexportar um componente de ../enrollment-domain.`);
    return errors;
  }

  if (exportDeclaration.moduleSpecifier !== '../enrollment-domain') {
    errors.push(
      `arquivo ${item.exposeFilePath} deve reexportar a partir de ../enrollment-domain, encontrado ${exportDeclaration.moduleSpecifier}.`,
    );
  }

  if (exportDeclaration.exportName !== item.exportName) {
    errors.push(
      `arquivo ${item.exposeFilePath} deve reexportar ${item.exportName}, encontrado ${exportDeclaration.exportName}.`,
    );
  }

  return errors;
}

function validateShellContract(manifestItems, shellEnrollmentRoutes, shellEnrollmentCandidates) {
  return [
    ...validateShellRoutes(manifestItems, shellEnrollmentRoutes),
    ...validateShellExtractionCandidate(
      manifestItems,
      shellEnrollmentRoutes,
      shellEnrollmentCandidates,
    ),
  ];
}

function validateShellRoutes(manifestItems, shellEnrollmentRoutes) {
  const errors = [];
  const [item] = manifestItems;

  if (shellEnrollmentRoutes.length !== 1) {
    errors.push(
      `shell do host deve expor 1 rota de matriculas, encontradas ${shellEnrollmentRoutes.length}.`,
    );
    return errors;
  }

  const [shellRoute] = shellEnrollmentRoutes;

  if (shellRoute.path !== item.path) {
    errors.push(`rota do shell deve usar path ${item.path}, encontrado ${shellRoute.path}.`);
  }

  if (shellRoute.domain !== item.domain) {
    errors.push(
      `rota ${item.path} do shell deve usar domain ${item.domain}, encontrado ${shellRoute.domain}.`,
    );
  }

  if (shellRoute.exposedModule !== item.exposedModule) {
    errors.push(
      `rota ${item.path} do shell deve usar exposedModule ${item.exposedModule}, encontrado ${shellRoute.exposedModule}.`,
    );
  }

  if (shellRoute.exportName !== item.exportName) {
    errors.push(
      `rota ${item.path} do shell deve usar exportName ${item.exportName}, encontrado ${shellRoute.exportName}.`,
    );
  }

  if (shellRoute.runtimeRemoteName !== 'mfe-matriculas') {
    errors.push(
      `rota ${item.path} do shell deve usar runtimeRemoteName mfe-matriculas, encontrado ${shellRoute.runtimeRemoteName}.`,
    );
  }

  const extractionPlan = shellRoute.extractionPlan;
  if (extractionPlan !== undefined) {
    errors.push(`rota ${item.path} do shell nao deve mais declarar extractionPlan apos o cutover.`);
  }

  return errors;
}

function validateShellExtractionCandidate(
  manifestItems,
  shellEnrollmentRoutes,
  shellEnrollmentCandidates,
) {
  const errors = [];
  const [item] = manifestItems;

  if (shellEnrollmentCandidates.length !== 0) {
    errors.push(
      `shell do host nao deve mais declarar candidato de extracao para matriculas, encontrados ${shellEnrollmentCandidates.length}.`,
    );
  }

  return errors;
}

function readExposeExport(sourceFile) {
  let result = null;

  for (const statement of sourceFile.statements) {
    if (!ts.isExportDeclaration(statement) || !statement.exportClause) {
      continue;
    }

    if (!ts.isNamedExports(statement.exportClause) || !statement.moduleSpecifier) {
      continue;
    }

    const [firstElement] = statement.exportClause.elements;
    if (!firstElement) {
      continue;
    }

    result = {
      exportName: firstElement.name.text,
      moduleSpecifier: statement.moduleSpecifier.text,
    };
    break;
  }

  return result;
}

function toSystemPath(relativePath) {
  return relativePath.replaceAll('/', path.sep).replace(/^\.\//, '');
}

function compareStringArrays(actual, expected, context) {
  if (!Array.isArray(actual)) {
    return [`${context} deve ser um array no shell do host.`];
  }

  if (actual.length !== expected.length) {
    return [
      `${context} deve conter ${expected.length} itens no shell do host, encontrados ${actual.length}.`,
    ];
  }

  const errors = [];

  for (let index = 0; index < expected.length; index += 1) {
    if (actual[index] !== expected[index]) {
      errors.push(
        `${context}[${index}] deve ser ${expected[index]} no shell do host, encontrado ${actual[index]}.`,
      );
    }
  }

  return errors;
}

function requireString(object, propertyName, context, errors) {
  if (typeof object[propertyName] !== 'string' || object[propertyName].trim() === '') {
    errors.push(`${context} deve definir ${propertyName} como string nao vazia.`);
  }
}

function requireBoolean(object, propertyName, context, errors) {
  if (typeof object[propertyName] !== 'boolean') {
    errors.push(`${context} deve definir ${propertyName} como boolean.`);
  }
}
