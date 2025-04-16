import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';
import { map, take, tap } from 'rxjs/operators';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.isLoggedIn$.pipe(
    take(1), // Take the current value and complete
    map(isLoggedIn => {
      if (isLoggedIn) {
        return true; // User is logged in, allow access
      } else {
        // User is not logged in, redirect to login page
        router.navigate(['/login'], { queryParams: { returnUrl: state.url } }); // Pass returnUrl for better UX
        return false; // Prevent access
      }
    })
  );
};
