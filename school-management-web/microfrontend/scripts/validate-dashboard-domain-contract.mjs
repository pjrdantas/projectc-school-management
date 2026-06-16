import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import ts from 'typescript';

const microfrontendRoot = process.cwd();
const manifestPath = path.join(
  microfrontendRoot,
  'src',
  'app',
  'dashboard',
  'dashboard-domain.manifest.ts',
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

const manifestItems = readArrayObjects(manifestSource, 'DASHBOARD_DOMAIN_MANIFEST');
const exposes = readExposes(federationSource);
const shellDashboardRoutes = readArrayObjects(shellSource, 'SHELL_REMOTE_ROUTES').filter(
  route => route.domain === 'dashboard',
);
const shellDashboardCandidates = readArrayObjects(
  shellSource,
  'SHELL_EXTRACTION_CANDIDATES',
).filter(candidate => candidate.domain === 'dashboard');

const errors = [
  ...validateManifestShape(manifestItems),
  ...validateFederation(manifestItems, exposes),
  ...validateExposeFiles(manifestItems),
  ...validateShellContract(
    manifestItems,
    shellDashboardRoutes,
    shellDashboardCandidates,
  ),
];

if (errors.length > 0) {
  console.error('Contrato interno do dominio dashboard invalido:');
  for (const error of errors) {
    console.error(`- ${error}`);
  }
  process.exit(1);
}

console.log(
  `Contrato interno do dashboard valido: ${manifestItems.length} entradas conferidas entre manifesto, exposes, federation.config.js e shell do host.`,
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
  const paths = new Set();
  const exposedModules = new Set();
  const exposeFilePaths = new Set();

  for (const item of manifestItems) {
    requireString(item, 'key', 'item do manifesto dashboard', errors);
    requireString(item, 'path', `manifesto dashboard ${item.key ?? '<sem-chave>'}`, errors);
    requireString(item, 'exposedModule', `manifesto dashboard ${item.key ?? '<sem-chave>'}`, errors);
    requireString(item, 'exposeFilePath', `manifesto dashboard ${item.key ?? '<sem-chave>'}`, errors);
    requireString(item, 'exportName', `manifesto dashboard ${item.key ?? '<sem-chave>'}`, errors);
    requireString(item, 'routeRole', `manifesto dashboard ${item.key ?? '<sem-chave>'}`, errors);
    requireString(item, 'shellNavigation', `manifesto dashboard ${item.key ?? '<sem-chave>'}`, errors);

    ensureUnique(keys, item.key, 'key', errors);
    ensureUnique(paths, item.path, 'path', errors);
    ensureUnique(exposedModules, item.exposedModule, 'exposedModule', errors);
    ensureUnique(exposeFilePaths, item.exposeFilePath, 'exposeFilePath', errors);
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
      errors.push(`arquivo ${item.exposeFilePath} deve reexportar um componente de ../dashboard-domain.`);
      continue;
    }

    if (exportDeclaration.moduleSpecifier !== '../dashboard-domain') {
      errors.push(
        `arquivo ${item.exposeFilePath} deve reexportar a partir de ../dashboard-domain, encontrado ${exportDeclaration.moduleSpecifier}.`,
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
  shellDashboardRoutes,
  shellDashboardCandidates,
) {
  return [
    ...validateShellRoutes(manifestItems, shellDashboardRoutes),
    ...validateShellExtractionCandidate(
      manifestItems,
      shellDashboardRoutes,
      shellDashboardCandidates,
    ),
  ];
}

function validateShellRoutes(manifestItems, shellDashboardRoutes) {
  const errors = [];

  if (shellDashboardRoutes.length !== manifestItems.length) {
    errors.push(
      `shell do host deve expor ${manifestItems.length} rotas de dashboard, encontradas ${shellDashboardRoutes.length}.`,
    );
  }

  for (const item of manifestItems) {
    const shellRoute = shellDashboardRoutes.find(route => route.path === item.path);

    if (!shellRoute) {
      errors.push(`shell do host nao declarou a rota ${item.path} para dashboard.`);
      continue;
    }

    if (shellRoute.domain !== 'dashboard') {
      errors.push(
        `rota ${item.path} do shell deve usar domain dashboard, encontrado ${shellRoute.domain}.`,
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

    if (extractionPlan.candidate !== true) {
      errors.push(
        `rota ${item.path} do shell deve usar extractionPlan.candidate=true, encontrado ${extractionPlan.candidate}.`,
      );
    }

    if (extractionPlan.targetRemoteName !== 'mfe-dashboard') {
      errors.push(
        `rota ${item.path} do shell deve usar extractionPlan.targetRemoteName=mfe-dashboard, encontrado ${extractionPlan.targetRemoteName}.`,
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
  shellDashboardRoutes,
  shellDashboardCandidates,
) {
  const errors = [];

  if (shellDashboardCandidates.length !== 1) {
    errors.push(
      `shell do host deve declarar um unico candidato de extracao para dashboard, encontrados ${shellDashboardCandidates.length}.`,
    );
    return errors;
  }

  const [candidate] = shellDashboardCandidates;
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
  const accessMenuRoutes = manifestItems
    .filter(item => item.shellNavigation === 'access-menu')
    .map(item => toShellMenuRoute(item.path));
  const runtimeRemoteNames = [
    ...new Set(shellDashboardRoutes.map(route => route.runtimeRemoteName)),
  ];

  if (candidate.currentPlacement !== 'microfrontend') {
    errors.push(
      `candidato dashboard do shell deve manter currentPlacement=microfrontend, encontrado ${candidate.currentPlacement}.`,
    );
  }

  if (candidate.targetRemoteName !== 'mfe-dashboard') {
    errors.push(
      `candidato dashboard do shell deve usar targetRemoteName=mfe-dashboard, encontrado ${candidate.targetRemoteName}.`,
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
    ...compareStringArrays(candidate.businessMenuRoutes, [], 'candidate.businessMenuRoutes'),
    ...compareStringArrays(
      candidate.accessMenuRoutes,
      accessMenuRoutes,
      'candidate.accessMenuRoutes',
    ),
    ...compareStringArrays(candidate.contextualRoutes, [], 'candidate.contextualRoutes'),
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
  return `/${routePath}`;
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
