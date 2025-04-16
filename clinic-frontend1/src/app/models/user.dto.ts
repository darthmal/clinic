// Matches relevant fields from com.clinicapp.backend.model.security.User
// Used for displaying user lists

export type UserRole = 'ADMIN' | 'DOCTOR' | 'SECRETARY';

export interface UserDTO {
  id: number;
  username: string; // Keep username if backend uses it, otherwise maybe remove
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  // Add other fields if needed for display (e.g., enabled status)
}

// Used for creating/updating users (password is optional for update)
export interface UserCreateUpdateDTO {
  id?: number; // Present for update, absent for create
  username?: string; // Required for create if backend uses it
  email: string;
  firstName: string;
  lastName: string;
  password?: string; // Required for create, optional for update
  role: UserRole;
  // Add other editable fields if needed
}