import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import ts from 'typescript';

const microfrontendRoot = process.cwd();
const manifestPath = path.join(
  microfrontendRoot,
  'src',
  'app',
  'professor',
  'professor-domain.manifest.ts',
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

const manifestItems = readArrayObjects(manifestSource, 'PROFESSOR_DOMAIN_MANIFEST');
const exposes = readExposes(federationSource);
const shellProfessorRoutes = readArrayObjects(shellSource, 'SHELL_REMOTE_ROUTES').filter(
  route => route.domain === 'professores',
);
const shellProfessorCandidates = readArrayObjects(
  shellSource,
  'SHELL_EXTRACTION_CANDIDATES',
).filter(candidate => candidate.domain === 'professores');

const errors = [
  ...validateManifestShape(manifestItems),
  ...validateFederation(manifestItems, exposes),
  ...validateExposeFiles(manifestItems),
  ...validateShellContract(
    manifestItems,
    shellProfessorRoutes,
    shellProfessorCandidates,
  ),
];

if (errors.length > 0) {
  console.error('Contrato interno do dominio professores invalido:');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(
  `Contrato interno de professores valido: ${manifestItems.length} entradas conferidas entre manifesto, exposes, federation.config.js e shell do host.`,
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
    requireString(item, 'key', 'item do manifesto professores', errors);
    requireString(item, 'domain', `manifesto professores ${item.key ?? '<sem-chave>'}`, errors);
    requireString(
      item,
      'futureRemoteName',
      `manifesto professores ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(item, 'path', `manifesto professores ${item.key ?? '<sem-chave>'}`, errors);
    requireString(
      item,
      'exposedModule',
      `manifesto professores ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(
      item,
      'exposeFilePath',
      `manifesto professores ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(
      item,
      'exportName',
      `manifesto professores ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(
      item,
      'routeKind',
      `manifesto professores ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(
      item,
      'routeRole',
      `manifesto professores ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireString(
      item,
      'shellNavigation',
      `manifesto professores ${item.key ?? '<sem-chave>'}`,
      errors,
    );
    requireBoolean(
      item,
      'extractionCandidate',
      `manifesto professores ${item.key ?? '<sem-chave>'}`,
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
    errors.push('manifesto professores deve manter um unico dominio interno.');
  }

  if (!domains.has('professores')) {
    errors.push('manifesto professores deve usar o dominio professores.');
  }

  if (futureRemoteNames.size > 1) {
    errors.push('manifesto professores deve manter um unico futureRemoteName.');
  }

  if (!futureRemoteNames.has('mfe-professores')) {
    errors.push('manifesto professores deve usar futureRemoteName mfe-professores.');
  }

  errors.push(...validateRouteClassification(manifestItems));

  return errors;
}

function validateRouteClassification(manifestItems) {
  const errors = [];

  for (const item of manifestItems) {
    if (item.extractionCandidate !== true) {
      errors.push(`rota ${item.path} deve permanecer marcada como extractionCandidate=true.`);
    }

    if (item.routeRole !== 'operational') {
      errors.push(`rota ${item.path} deve marcar routeRole como operational.`);
    }

    if (item.path === 'teachers') {
      if (item.routeKind !== 'list') {
        errors.push(`rota ${item.path} deve marcar routeKind como list.`);
      }

      if (item.shellNavigation !== 'business-menu') {
        errors.push(`rota ${item.path} deve marcar shellNavigation como business-menu.`);
      }
    }

    if (item.path === 'teachers/:id') {
      if (item.routeKind !== 'detail') {
        errors.push(`rota ${item.path} deve marcar routeKind como detail.`);
      }

      if (item.shellNavigation !== 'contextual') {
        errors.push(`rota ${item.path} deve marcar shellNavigation como contextual.`);
      }
    }
  }

  return errors;
}

function validateFederation(manifestItems, exposes) {
  const errors = [];

  for (const item of manifestItems) {
    const federationExposePath = exposes.get(item.exposedModule);
    if (!federationExposePath) {
      errors.push(`federation.config.js nao expõe o modulo ${item.exposedModule}.`);
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
      errors.push(`arquivo ${item.exposeFilePath} deve reexportar um componente de ../professor-domain.`);
      continue;
    }

    if (exportDeclaration.moduleSpecifier !== '../professor-domain') {
      errors.push(
        `arquivo ${item.exposeFilePath} deve reexportar a partir de ../professor-domain, encontrado ${exportDeclaration.moduleSpecifier}.`,
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

function validateShellContract(
  manifestItems,
  shellProfessorRoutes,
  shellProfessorCandidates,
) {
  return [
    ...validateShellRoutes(manifestItems, shellProfessorRoutes),
    ...validateShellExtractionCandidate(
      manifestItems,
      shellProfessorRoutes,
      shellProfessorCandidates,
    ),
  ];
}

function validateShellRoutes(manifestItems, shellProfessorRoutes) {
  const errors = [];

  if (shellProfessorRoutes.length !== manifestItems.length) {
    errors.push(
      `shell do host deve expor ${manifestItems.length} rotas de professores, encontradas ${shellProfessorRoutes.length}.`,
    );
  }

  for (const item of manifestItems) {
    const shellRoute = shellProfessorRoutes.find(route => route.path === item.path);

    if (!shellRoute) {
      errors.push(`shell do host nao declarou a rota ${item.path} para professores.`);
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

    if (shellRoute.runtimeRemoteName !== 'mfe1') {
      errors.push(
        `rota ${item.path} do shell deve permanecer no runtimeRemoteName mfe1, encontrado ${shellRoute.runtimeRemoteName}.`,
      );
    }

    const extractionPlan = shellRoute.extractionPlan;
    if (!extractionPlan || typeof extractionPlan !== 'object') {
      errors.push(`rota ${item.path} do shell deve declarar extractionPlan.`);
      continue;
    }

    if (extractionPlan.candidate !== item.extractionCandidate) {
      errors.push(
        `rota ${item.path} do shell deve usar extractionPlan.candidate=${item.extractionCandidate}, encontrado ${extractionPlan.candidate}.`,
      );
    }

    if (extractionPlan.targetRemoteName !== item.futureRemoteName) {
      errors.push(
        `rota ${item.path} do shell deve usar extractionPlan.targetRemoteName=${item.futureRemoteName}, encontrado ${extractionPlan.targetRemoteName}.`,
      );
    }

    if (extractionPlan.routeRole !== item.routeRole) {
      errors.push(
        `rota ${item.path} do shell deve usar extractionPlan.routeRole=${item.routeRole}, encontrado ${extractionPlan.routeRole}.`,
      );
    }

    if (extractionPlan.shellNavigation !== item.shellNavigation) {
      errors.push(
        `rota ${item.path} do shell deve usar extractionPlan.shellNavigation=${item.shellNavigation}, encontrado ${extractionPlan.shellNavigation}.`,
      );
    }
  }

  return errors;
}

function validateShellExtractionCandidate(
  manifestItems,
  shellProfessorRoutes,
  shellProfessorCandidates,
) {
  const errors = [];

  if (shellProfessorCandidates.length !== 1) {
    errors.push(
      `shell do host deve declarar um unico candidato de extracao para professores, encontrados ${shellProfessorCandidates.length}.`,
    );
    return errors;
  }

  const [candidate] = shellProfessorCandidates;
  const manifestPaths = manifestItems.map(item => item.path);
  const manifestExposedModules = manifestItems.map(item => item.exposedModule);
  const operationalRoutePaths = manifestItems
    .filter(item => item.routeRole === 'operational')
    .map(item => item.path);
  const administrativeRoutePaths = manifestItems
    .filter(item => item.routeRole === 'administrative')
    .map(item => item.path);
  const landingRoutes = manifestItems
    .filter(item => item.shellNavigation === 'landing')
    .map(item => item.path);
  const businessMenuRoutes = manifestItems
    .filter(item => item.shellNavigation === 'business-menu')
    .map(item => toShellMenuRoute(item.path));
  const accessMenuRoutes = manifestItems
    .filter(item => item.shellNavigation === 'access-menu')
    .map(item => toShellMenuRoute(item.path));
  const contextualRoutes = manifestItems
    .filter(item => item.shellNavigation === 'contextual')
    .map(item => item.path);
  const runtimeRemoteNames = [
    ...new Set(shellProfessorRoutes.map(route => route.runtimeRemoteName)),
  ];

  if (candidate.currentPlacement !== 'microfrontend') {
    errors.push(
      `candidato professores do shell deve manter currentPlacement=microfrontend, encontrado ${candidate.currentPlacement}.`,
    );
  }

  if (candidate.targetRemoteName !== manifestItems[0]?.futureRemoteName) {
    errors.push(
      `candidato professores do shell deve usar targetRemoteName=${manifestItems[0]?.futureRemoteName}, encontrado ${candidate.targetRemoteName}.`,
    );
  }

  errors.push(
    ...compareStringArrays(
      candidate.runtimeRemoteNames,
      runtimeRemoteNames,
      'candidate.runtimeRemoteNames',
    ),
    ...compareStringArrays(
      candidate.expectedExposedModules,
      manifestExposedModules,
      'candidate.expectedExposedModules',
    ),
    ...compareStringArrays(candidate.routePaths, manifestPaths, 'candidate.routePaths'),
    ...compareStringArrays(
      candidate.operationalRoutePaths,
      operationalRoutePaths,
      'candidate.operationalRoutePaths',
    ),
    ...compareStringArrays(
      candidate.administrativeRoutePaths,
      administrativeRoutePaths,
      'candidate.administrativeRoutePaths',
    ),
    ...compareStringArrays(candidate.landingRoutes, landingRoutes, 'candidate.landingRoutes'),
    ...compareStringArrays(
      candidate.businessMenuRoutes,
      businessMenuRoutes,
      'candidate.businessMenuRoutes',
    ),
    ...compareStringArrays(
      candidate.accessMenuRoutes,
      accessMenuRoutes,
      'candidate.accessMenuRoutes',
    ),
    ...compareStringArrays(
      candidate.contextualRoutes,
      contextualRoutes,
      'candidate.contextualRoutes',
    ),
  );

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

function toShellMenuRoute(routePath) {
  return `/${routePath.split('/:')[0]}`;
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
