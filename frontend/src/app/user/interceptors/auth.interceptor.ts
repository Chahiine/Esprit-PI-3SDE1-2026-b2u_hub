import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Ne pas toucher les appels externes (OpenRouter etc.)
  if (!req.url.includes('localhost:8081')) return next(req);

  const token = localStorage.getItem('b2u_token');
  if (token) {
    return next(req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    }));
  }
  return next(req);
};
