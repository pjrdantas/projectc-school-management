import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ShellContextService } from './shell-context.service';

export const shellAuthInterceptor: HttpInterceptorFn = (req, next) => {
  const shellContext = inject(ShellContextService);
  const token = shellContext.getToken();

  return next(
    token
      ? req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          },
        })
      : req,
  );
};

