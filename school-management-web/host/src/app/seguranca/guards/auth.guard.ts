import { inject } from '@angular/core';
import { CanMatchFn, Router, UrlSegment } from '@angular/router';
import { AuthStateService } from '../../core/auth/auth-state.service';

export const authGuard: CanMatchFn = (_route, segments: UrlSegment[]) => {
  const authState = inject(AuthStateService);
  const router = inject(Router);

  if (authState.getToken()) {
    return true;
  }

  const returnUrl = segments.map(segment => segment.path).join('/');
  return router.createUrlTree(['/login'], {
    queryParams: returnUrl ? { returnUrl: `/${returnUrl}` } : undefined,
  });
};

export const guestGuard: CanMatchFn = () => {
  const authState = inject(AuthStateService);
  const router = inject(Router);

  if (!authState.getToken()) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
