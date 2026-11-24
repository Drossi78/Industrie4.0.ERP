import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { AuthSessionService } from '../../features/auth/services/auth-session.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = inject(AuthSessionService).getAccessToken();
  if (!token) {
    return next(req);
  }
  return next(
    req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }),
  );
};
