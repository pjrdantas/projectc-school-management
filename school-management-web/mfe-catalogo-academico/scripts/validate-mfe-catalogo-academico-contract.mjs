import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import ts from 'typescript';

const microfrontendRoot = process.cwd();
const manifestPath = path.join(
  microfrontendRoot,
  'src',
  'app',
  'catalogo',
  'catalog-domain.manifest.ts',
);
const federationPath = path.join(microfrontendRoot, 'federation.config.js');
const shellNavigationPath = path.join(
  microfrontendRoot,
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

const manifestItems = readArrayObjects(manifestSource, 'CATALOG_DOMAIN_MANIFEST');
const exposes = readExposes(federationSource);
const shellCatalogRoutes = readArrayObjects(shellSource, 'SHELL_REMOTE_ROUTES').filter(
  route => route.domain === 'catalogo-academico',
);
const shellCatalogCandidates = readArrayObjects(
  shellSource,
  'SHELL_EXTRACTION_CANDIDATES',
).filter(candidate => candidate.domain === 'catalogo-academico');

const errors = [
  ...validateManifestShape(manifestItems),
  ...validateFederation(manifestItems, exposes),
  ...validateExposeFiles(manifestItems),
  ...validateShellContract(manifestItems, shellCatalogRoutes, shellCatalogCandidates),
];

if (errors.length > 0) {
  console.error('Contrato do mfe-catalogo-academico invalido:');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(
  `Contrato do mfe-catalogo-academico valido: ${manifestItems.length} entradas conferidas entre manifesto, exposes locais, federation.config.js e shell do host.`,
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
  const keys = new Set();
  const domains = new Set();
  const futureRemoteNames = new Set();
  const paths = new Set();
  const exposedModules = new Set();
  const exposeFilePaths = new Set();

  for (const item of manifestItems) {
    requireString(item, 'key', 'item do manifesto catalogo academico', errors);
    requireString(item, 'domain', `manifesto catalogo academico ${item.key ?? '<sem-chave>'}`, errors);
    requireString(
      item,
      'futureRemoteName',
      `manifesto catalogo academico ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(item, 'path', `manifesto catalogo academico ${item.key ?? '<sem-chave>'}`, errors);
    requireString(
      item,
      'exposedModule',
      `manifesto catalogo academico ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(
      item,
      'exposeFilePath',
      `manifesto catalogo academico ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(
      item,
      'exportName',
      `manifesto catalogo academico ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(
      item,
      'routeKind',
      `manifesto catalogo academico ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(
      item,
      'routeRole',
      `manifesto catalogo academico ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(
      item,
      'shellNavigation',
      `manifesto catalogo academico ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireBoolean(
      item,
      'extractionCandidate',
      `manifesto catalogo academico ${item.key ?? '<sem-chave>'}`,
      errors,
    );

    ensureUnique(keys, item.key, 'key', errors);
    domains.add(item.domain);
    futureRemoteNames.add(item.futureRemoteName);
    ensureUnique(paths, item.path, 'path', errors);
    ensureUnique(exposedModules, item.exposedModule, 'exposedModule', errors);
    ensureUnique(exposeFilePaths, item.exposeFilePath, 'exposeFilePath', errors);
  }

  if (domains.size > 1) {
    errors.push('manifesto catalogo academico deve manter um unico dominio interno.');
  }

  if (!domains.has('catalogo-academico')) {
    errors.push('manifesto catalogo academico deve usar o dominio catalogo-academico.');
  }

  if (futureRemoteNames.size > 1) {
    errors.push('manifesto catalogo academico deve manter um unico futureRemoteName.');
  }

  if (!futureRemoteNames.has('mfe-catalogo-academico')) {
    errors.push(
      'manifesto catalogo academico deve usar futureRemoteName mfe-catalogo-academico.',
    );
  }

  for (const item of manifestItems) {
    if (item.extractionCandidate !== false) {
      errors.push(`rota ${item.path} deve marcar extractionCandidate=false no remote dedicado.`);
    }

    if (item.routeKind !== 'list') {
      errors.push(`rota ${item.path} deve marcar routeKind como list.`);
    }

    if (item.routeRole !== 'operational') {
      errors.push(`rota ${item.path} deve marcar routeRole como operational.`);
    }

    if (item.shellNavigation !== 'business-menu') {
      errors.push(`rota ${item.path} deve marcar shellNavigation como business-menu.`);
    }
  }

  return errors;
}

function validateFederation(manifestItems, exposes) {
  const errors = [];

  for (const item of manifestItems) {
    const federationExposePath = exposes.get(item.exposedModule);

    if (!federationExposePath) {
      errors.push(`federation.config.js nao expoe o modulo ${item.exposedModule}.`);
      continue;
    }

    if (federationExposePath !== item.exposeFilePath) {
      errors.push(
        `modulo ${item.exposedModule} deve apontar para ${item.exposeFilePath} em federation.config.js, encontrado ${federationExposePath}.`,
      );
    }
  }

  return errors;
}

function validateExposeFiles(manifestItems) {
  const errors = [];

  for (const item of manifestItems) {
    const exposeFilePath = path.join(microfrontendRoot, toSystemPath(item.exposeFilePath));

    if (!fs.existsSync(exposeFilePath)) {
      errors.push(`arquivo de expose ausente para ${item.exposedModule}: ${item.exposeFilePath}.`);
      continue;
    }

    const exposeSource = parseSource(exposeFilePath, ts.ScriptKind.TS);
    const exportDeclaration = readExposeExport(exposeSource);

    if (!exportDeclaration) {
      errors.push(`arquivo ${item.exposeFilePath} deve reexportar um componente de ../catalog-domain.`);
      continue;
    }

    if (exportDeclaration.moduleSpecifier !== '../catalog-domain') {
      errors.push(
        `arquivo ${item.exposeFilePath} deve reexportar a partir de ../catalog-domain, encontrado ${exportDeclaration.moduleSpecifier}.`,
      );
    }

    if (exportDeclaration.exportName !== item.exportName) {
      errors.push(
        `arquivo ${item.exposeFilePath} deve reexportar ${item.exportName}, encontrado ${exportDeclaration.exportName}.`,
      );
    }
  }

  return errors;
}

function validateShellContract(manifestItems, shellCatalogRoutes, shellCatalogCandidates) {
  return [
    ...validateShellRoutes(manifestItems, shellCatalogRoutes),
    ...validateShellExtractionCandidate(shellCatalogCandidates),
  ];
}

function validateShellRoutes(manifestItems, shellCatalogRoutes) {
  const errors = [];

  if (shellCatalogRoutes.length !== manifestItems.length) {
    errors.push(
      `shell do host deve expor ${manifestItems.length} rotas de catalogo academico, encontradas ${shellCatalogRoutes.length}.`,
    );
  }

  for (const item of manifestItems) {
    const shellRoute = shellCatalogRoutes.find(route => route.path === item.path);

    if (!shellRoute) {
      errors.push(`shell do host nao declarou a rota ${item.path} para catalogo academico.`);
      continue;
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

    if (shellRoute.runtimeRemoteName !== 'mfe-catalogo-academico') {
      errors.push(
        `rota ${item.path} do shell deve usar runtimeRemoteName mfe-catalogo-academico, encontrado ${shellRoute.runtimeRemoteName}.`,
      );
    }

    if (shellRoute.extractionPlan !== undefined) {
      errors.push(`rota ${item.path} do shell nao deve mais declarar extractionPlan apos o cutover.`);
    }
  }

  return errors;
}

function validateShellExtractionCandidate(shellCatalogCandidates) {
  const errors = [];

  if (shellCatalogCandidates.length !== 0) {
    errors.push(
      `shell do host nao deve mais declarar candidato de extracao para catalogo academico, encontrados ${shellCatalogCandidates.length}.`,
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

function ensureUnique(seen, value, propertyName, errors) {
  if (typeof value !== 'string' || value.trim() === '') {
    return;
  }

  if (seen.has(value)) {
    errors.push(`valor duplicado para ${propertyName}: ${value}.`);
    return;
  }

  seen.add(value);
}
