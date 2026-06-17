import { initFederation } from '@angular-architects/native-federation';
import { publishShellRouteCutovers } from './app/core/shell/shell-remote-runtime';

(globalThis as any).ngDevMode ??= false;

type FederationManifest = Record<string, string>;
type ShellRouteCutoverMap = Record<string, string>;

function withCacheBust(url: string, version: number): string {
  const separator = url.includes('?') ? '&' : '?';
  return `${url}${separator}v=${version}`;
}

async function loadFederationManifest(): Promise<FederationManifest> {
  const version = Date.now();
  const response = await fetch(withCacheBust('federation.manifest.json', version), {
    cache: 'no-store',
  });

  if (!response.ok) {
    throw new Error(`Erro ao carregar manifesto federation: ${response.status}`);
  }

  const manifest = (await response.json()) as FederationManifest;
  const standbyManifest = await loadJson<FederationManifest>(
    'federation.standby.manifest.json',
    version,
  );
  const routeCutovers = await loadJson<ShellRouteCutoverMap>('shell-route-cutovers.json', version);
  const selectedManifest = buildSelectedManifest(manifest, standbyManifest, routeCutovers);

  publishShellRouteCutovers(routeCutovers);

  await Promise.all(
    Object.values(selectedManifest).map(remoteEntryUrl =>
      fetch(remoteEntryUrl, { cache: 'reload' }),
    ),
  );

  return selectedManifest;
}

async function loadJson<T>(relativePath: string, version: number): Promise<T> {
  const response = await fetch(withCacheBust(relativePath, version), {
    cache: 'no-store',
  });

  if (!response.ok) {
    throw new Error(`Erro ao carregar ${relativePath}: ${response.status}`);
  }

  return (await response.json()) as T;
}

function buildSelectedManifest(
  activeManifest: FederationManifest,
  standbyManifest: FederationManifest,
  routeCutovers: ShellRouteCutoverMap,
): FederationManifest {
  const selectedManifest: FederationManifest = { ...activeManifest };
  const selectedRemoteNames = [...new Set(Object.values(routeCutovers).filter(Boolean))];

  for (const remoteName of selectedRemoteNames) {
    if (selectedManifest[remoteName]) {
      continue;
    }

    const standbyRemoteEntry = standbyManifest[remoteName];
    if (!standbyRemoteEntry) {
      throw new Error(`Remote ${remoteName} nao foi encontrado nos manifestos do shell.`);
    }

    selectedManifest[remoteName] = standbyRemoteEntry;
  }

  return selectedManifest;
}

loadFederationManifest()
  .then(remotes => initFederation(remotes))
  .catch((err: unknown) => console.error('Erro ao inicializar federation', err))
  .then(() => import('./bootstrap'))
  .catch((err: unknown) => console.error('Erro ao carregar bootstrap', err));
