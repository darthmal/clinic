import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimations } from '@angular/platform-browser/animations'; // Import animations provider
import { provideToastr } from 'ngx-toastr'; // Import toastr provider

import { authInterceptor } from './interceptors/auth.interceptor';
import { AppRoutingModule } from './app-routing.module';

// Remove default routes import if using AppRoutingModule
// import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    // provideRouter(routes), // Use AppRoutingModule instead if it defines routes
    importProvidersFrom(AppRoutingModule), // Provide routes from AppRoutingModule
    provideHttpClient(withInterceptors([authInterceptor])),
    provideAnimations(), // Add animations provider
    provideToastr({ // Add toastr provider with options
      timeOut: 5000, // Default time to close
      positionClass: 'toast-top-right', // Position on screen
      preventDuplicates: true,
    }),
  ]
};
