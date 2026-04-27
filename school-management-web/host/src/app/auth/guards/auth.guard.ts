import { inject } from '@angular/core';
import { CanMatchFn, Router, UrlSegment } from '@angular/router';
import { AuthSessionService } from '../services/auth-session.service';

export const authGuard: CanMatchFn = (_route, segments: UrlSegment[]) => {
  const authSession = inject(AuthSessionService);
  const router = inject(Router);

  if (authSession.isAuthenticated()) {
    return true;
  }

  const returnUrl = segments.map(segment => segment.path).join('/');
  return router.createUrlTree(['/auth/login'], {
    queryParams: returnUrl ? { returnUrl: `/${returnUrl}` } : undefined,
  });
};

export const guestGuard: CanMatchFn = () => {
  const authSession = inject(AuthSessionService);
  const router = inject(Router);

  if (!authSession.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree(['/home']);
};
