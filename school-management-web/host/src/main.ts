import { initFederation } from '@angular-architects/native-federation';

(globalThis as any).ngDevMode ??= false;

type FederationManifest = Record<string, string>;

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

  const manifest = await response.json() as FederationManifest;

  await Promise.all(
    Object.values(manifest).map(remoteEntryUrl =>
      fetch(remoteEntryUrl, { cache: 'reload' }),
    ),
  );

  return manifest;
}

loadFederationManifest()
  .then(remotes => initFederation(remotes))
  .catch((err: unknown) => console.error('Erro ao inicializar federation', err))
  .then(() => import('./bootstrap'))
  .catch((err: unknown) => console.error('Erro ao carregar bootstrap', err));
