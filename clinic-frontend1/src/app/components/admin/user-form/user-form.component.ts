import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../../services/admin/user.service';
import { UserDTO, UserCreateUpdateDTO, UserRole } from '../../../models/user.dto'; // Import UserDTO as well

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './user-form.component.html',
  styleUrls: ['./user-form.component.css']
})
export class UserFormComponent implements OnInit {
  userForm!: FormGroup;
  isEditMode = false;
  userId: number | null = null;
  isLoading = false;
  errorMessage: string | null = null;
  pageTitle = 'Create New User';
  availableRoles: UserRole[] = ['DOCTOR', 'SECRETARY']; // Only allow creating/editing these roles via UI

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.userForm = this.fb.group({
      username: ['', Validators.required], // Uncomment and require username
      email: ['', [Validators.required, Validators.email]],
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      role: ['DOCTOR', Validators.required], // Default role
      // Password required only for create mode, optional for edit
      password: ['']
    });

    // Check for edit mode
    this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.isEditMode = true;
        this.userId = +id;
        this.pageTitle = 'Edit User';
        // Add password validator only if creating
        if (!this.isEditMode) {
           this.userForm.get('password')?.setValidators(Validators.required);
        } else {
           // Password is optional for edit, remove required validator if set
           this.userForm.get('password')?.clearValidators();
        }
        this.userForm.get('password')?.updateValueAndValidity(); // Update validation status

        this.loadUserData(this.userId);
      } else {
         // Ensure password is required for create mode
         this.userForm.get('password')?.setValidators(Validators.required);
         this.userForm.get('password')?.updateValueAndValidity();
      }
    });
  }

  loadUserData(id: number): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.userService.getUserById(id).subscribe({
      next: (user: UserDTO) => { // Explicitly type the received user
        this.isLoading = false;
        // Patch form with received data (UserDTO doesn't have password)
        this.userForm.patchValue(user); // Keep this correct line
        // Remove the erroneous line below
        // Cannot patch password field for security reasons
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = 'Failed to load user data.';
        console.error('Error loading user:', err);
        // Handle specific errors (e.g., 403 Forbidden, 404 Not Found)
         if (err.status === 403) {
            this.errorMessage = 'Access Denied: You do not have permission to view this user.';
         } else if (err.status === 404) {
             this.errorMessage = 'User not found.';
         }
      }
    });
  }

  onSubmit(): void {
    if (this.userForm.invalid) {
      this.errorMessage = 'Please fill in all required fields correctly.';
      this.userForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = null;
    const userData: UserCreateUpdateDTO = this.userForm.value;

    // Remove password if it's empty during edit mode
    if (this.isEditMode && !userData.password?.trim()) {
       delete userData.password;
    }

    if (this.isEditMode && this.userId) {
      // --- Update User ---
      this.userService.updateUser(this.userId, userData).subscribe({
        next: () => {
          this.isLoading = false;
          console.log('User updated successfully');
          this.router.navigate(['/admin/users']); // Navigate back to list
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = this.extractBackendErrorMessage(err, 'Failed to update user.');
          console.error('Error updating user:', err);
        }
      });
    } else {
      // --- Create User ---
      this.userService.createUser(userData).subscribe({
        next: () => {
          this.isLoading = false;
          console.log('User created successfully');
          this.router.navigate(['/admin/users']); // Navigate back to list
        },
        error: (err) => {
          this.isLoading = false;
           this.errorMessage = this.extractBackendErrorMessage(err, 'Failed to create user.');
          console.error('Error creating user:', err);
        }
      });
    }
  }

  // Helper to try and extract specific error messages from backend response
  private extractBackendErrorMessage(error: any, defaultMessage: string): string {
     if (error?.error?.message) { // Check if backend sends error in { "message": "..." } format
        return error.error.message;
     }
     if (error?.message) {
        return error.message;
     }
     // Handle specific statuses
     if (error.status === 403) {
        return 'Access Denied: You do not have permission to perform this action.';
     }
     if (error.status === 400 && error?.error?.errors) { // Example: Handle validation errors if backend sends them
        // Process validation errors if needed
        return 'Validation failed. Please check the form fields.';
     }
     return defaultMessage;
  }


  goBack(): void {
    this.router.navigate(['/admin/users']);
  }
}
