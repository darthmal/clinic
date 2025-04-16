import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router'; // Import RouterLink
import { Observable } from 'rxjs';
import { UserDTO, UserRole } from '../../../models/user.dto'; // Import DTOs
import { UserService } from '../../../services/admin/user.service'; // Import UserService

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterLink], // Add RouterLink
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.css']
})
export class UserListComponent implements OnInit {

  users$: Observable<UserDTO[]> | undefined;
  isLoading = false;
  errorMessage: string | null = null;
  // Optional: Add filter state if implementing filtering UI
  // selectedRoleFilter: UserRole | null = null;

  constructor(
    private userService: UserService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(role?: UserRole): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.users$ = this.userService.getUsers(role); // Pass filter if implemented
    this.users$.subscribe({
       next: () => this.isLoading = false,
       error: (err) => {
         this.isLoading = false;
         this.errorMessage = 'Failed to load users. Please try again.';
         console.error('Error loading users:', err);
         // Handle specific errors (e.g., 403 Forbidden if not Admin)
         if (err.status === 403) {
            this.errorMessage = 'Access Denied: You do not have permission to view users.';
         }
       }
    });
  }

  // Optional: Method to apply filter from UI
  // applyFilter(role: UserRole | null): void {
  //   this.selectedRoleFilter = role;
  //   this.loadUsers(this.selectedRoleFilter ?? undefined);
  // }

  editUser(id: number): void {
    this.router.navigate(['/admin/users/edit', id]); // Navigate to edit form
  }

  deleteUser(id: number): void {
    if (confirm('Are you sure you want to delete this user? This action cannot be undone.')) {
      this.isLoading = true; // Show loading indicator during delete
      this.userService.deleteUser(id).subscribe({
        next: () => {
          console.log(`User ${id} deleted successfully.`);
          this.loadUsers(); // Refresh the list after deletion
        },
        error: (err) => {
          this.isLoading = false;
          this.errorMessage = 'Failed to delete user.';
          console.error('Error deleting user:', err);
           // Handle specific errors (e.g., 403 Forbidden)
           if (err.status === 403) {
              this.errorMessage = 'Access Denied: You do not have permission to delete users.';
           }
        }
      });
    }
  }
}
