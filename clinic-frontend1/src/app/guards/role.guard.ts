import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';
import { map, take } from 'rxjs/operators';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const expectedRoles = route.data['roles'] as Array<'ADMIN' | 'DOCTOR' | 'SECRETARY'>; // Get expected roles from route data

  if (!expectedRoles || expectedRoles.length === 0) {
    console.warn('RoleGuard: No roles defined for this route. Allowing access.');
    return true; // Or handle as an error, depending on requirements
  }

  return authService.currentUser$.pipe(
    take(1), // Get the current user value
    map(user => {
      if (!user) {
        console.error('RoleGuard: User not logged in.');
        router.navigate(['/login']); // Should be handled by AuthGuard, but good practice
        return false;
      }

      const hasRole = expectedRoles.some(role => user.role === role);

      if (hasRole) {
        return true; // User has one of the required roles
      } else {
        console.warn(`RoleGuard: User role '${user.role}' not authorized for route requiring roles: ${expectedRoles.join(', ')}`);
        // Redirect to a generic dashboard or an 'unauthorized' page
        // For now, redirecting to a base dashboard path (we might need a dedicated component later)
        router.navigate(['/dashboard']); // Or perhaps '/unauthorized'
        return false; // Prevent access
      }
    })
  );
};
