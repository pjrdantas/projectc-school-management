export const ADMIN_PERMISSION = 'ADMIN';

export function hasPermission(userPermissions: string[], permission: string): boolean {
  return userPermissions.includes(permission) || userPermissions.includes(ADMIN_PERMISSION);
}
