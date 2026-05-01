import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthStateService } from '../core/auth/auth-state.service';

export const authGuard: CanActivateFn = () => {
  const authState = inject(AuthStateService);
  const router = inject(Router);

  const token = authState.getToken();

  if (!token) {
    router.navigate(['/login']);
    return false;
  }

  return true;
};
