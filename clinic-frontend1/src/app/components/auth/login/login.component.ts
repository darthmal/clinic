import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../services/auth/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // Import necessary modules
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup; // Use definite assignment assertion
  errorMessage: string | null = null;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.errorMessage = 'Please enter valid email and password.';
      return;
    }

    this.errorMessage = null;
    this.isLoading = true;

    const credentials = this.loginForm.value;

    this.authService.login(credentials).subscribe({
      next: (response) => {
        this.isLoading = false;
        // Navigate based on role after successful login
        const role = response.user.role;
        if (role === 'ADMIN') {
          this.router.navigate(['/admin-dashboard']); // Correct route
        } else if (role === 'DOCTOR') {
          this.router.navigate(['/doctor-dashboard']); // Correct route
        } else if (role === 'SECRETARY') {
          this.router.navigate(['/secretary-dashboard']); // Correct route
        } else {
          // Decide on a sensible default, maybe appointments or a generic landing page within the layout
          console.warn("Logged in user has unknown role or no specific dashboard route defined, navigating to appointments.");
          this.router.navigate(['/appointments']); // Example fallback
        }
      },
      error: (error) => {
        this.isLoading = false;
        // Handle specific error messages from backend if available
        if (error.status === 401 || error.status === 403) {
          this.errorMessage = 'Invalid email or password.';
        } else {
          this.errorMessage = 'An error occurred during login. Please try again.';
        }
        console.error('Login error:', error);
      }
    });
  }
}
