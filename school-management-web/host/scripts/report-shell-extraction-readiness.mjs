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

const shellSource = parseSource(shellNavigationPath, ts.ScriptKind.TS);
const extractionCandidates = readArrayObjects(shellSource, 'SHELL_EXTRACTION_CANDIDATES');
const domainCatalog = readObjectMap(shellSource, 'SHELL_DOMAIN_CATALOG');

const rankedCandidates = extractionCandidates
  .map(candidate => ({
    ...candidate,
    score: calculateComplexityScore(candidate),
    classification: classifyCandidate(candidate),
  }))
  .sort(compareCandidates);

if (rankedCandidates.length === 0) {
  console.log('Nenhum candidato de extracao foi declarado no shell.');
  process.exit(0);
}

const [recommendedCandidate] = rankedCandidates;

console.log('Resumo de prontidao de extracao do shell:');

rankedCandidates.forEach((candidate, index) => {
  const catalogItem = domainCatalog.get(candidate.domain);
  const label = catalogItem?.label ?? candidate.label;
  console.log(
    `${index + 1}. ${candidate.domain} (${label}) -> ${candidate.targetRemoteName} | classificacao=${candidate.classification} | rotas=${candidate.routePaths.length} | exposes=${candidate.expectedExposedModules.length} | menu-negocio=${candidate.businessMenuRoutes.length} | contextuais=${candidate.contextualRoutes.length} | administrativas=${candidate.administrativeRoutePaths.length} | score=${candidate.score}`,
  );
});

console.log('');
console.log(
  `Recorte inicial recomendado: ${recommendedCandidate.domain} -> ${recommendedCandidate.targetRemoteName}.`,
);
console.log(
  buildRecommendationReason(recommendedCandidate),
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

function calculateComplexityScore(candidate) {
  return (
    candidate.routePaths.length * 100 +
    candidate.contextualRoutes.length * 30 +
    candidate.administrativeRoutePaths.length * 40 +
    candidate.accessMenuRoutes.length * 20 +
    candidate.landingRoutes.length * 15 +
    candidate.businessMenuRoutes.length * 5
  );
}

function classifyCandidate(candidate) {
  if (
    candidate.routePaths.length === 1 &&
    candidate.contextualRoutes.length === 0 &&
    candidate.administrativeRoutePaths.length === 0 &&
    candidate.accessMenuRoutes.length === 0 &&
    candidate.landingRoutes.length === 0
  ) {
    return 'ready-first';
  }

  if (
    candidate.administrativeRoutePaths.length === 0 &&
    candidate.accessMenuRoutes.length === 0
  ) {
    return 'ready-next';
  }

  return 'ready-later';
}

function compareCandidates(left, right) {
  if (left.score !== right.score) {
    return left.score - right.score;
  }

  if (left.routePaths.length !== right.routePaths.length) {
    return left.routePaths.length - right.routePaths.length;
  }

  return left.domain.localeCompare(right.domain);
}

function buildRecommendationReason(candidate) {
  return [
    `Motivo: ${candidate.domain} tem a menor superficie contratual no shell atual, com ${candidate.routePaths.length} rota(s), ${candidate.expectedExposedModules.length} expose(s), ${candidate.contextualRoutes.length} rota(s) contextual(is) e ${candidate.administrativeRoutePaths.length} rota(s) administrativa(s).`,
    'Isso reduz o risco do primeiro corte quando a criacao do novo microfrontend for autorizada.',
  ].join(' ');
}
