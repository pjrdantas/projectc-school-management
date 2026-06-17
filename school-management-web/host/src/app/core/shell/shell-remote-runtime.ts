type ShellRouteCutoverMap = Record<string, string>;

declare global {
  interface Window {
    __shellRouteCutovers__?: ShellRouteCutoverMap;
  }
}

function readCutovers(): ShellRouteCutoverMap {
  return window.__shellRouteCutovers__ ?? {};
}

export function resolveShellRouteRemoteName(path: string, fallbackRemoteName: string): string {
  const configuredRemoteName = readCutovers()[path];
  return configuredRemoteName?.trim() || fallbackRemoteName;
}

export function publishShellRouteCutovers(cutovers: ShellRouteCutoverMap): void {
  window.__shellRouteCutovers__ = cutovers;
}

