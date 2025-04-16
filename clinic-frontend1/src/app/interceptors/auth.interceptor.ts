import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth/auth.service';
import { environment } from '../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const token = authService.getToken();
  const apiUrl = environment.apiUrl; // Get API URL from environment

  // Check if the request URL starts with the API URL and a token exists
  if (token && req.url.startsWith(apiUrl)) {
    // Clone the request and add the authorization header
    const clonedReq = req.clone({
      headers: req.headers.set('Authorization', `Bearer ${token}`)
    });
    // Pass the cloned request to the next handler
    return next(clonedReq);
  }

  // If no token or request is not for our API, pass the original request
  return next(req);
};
