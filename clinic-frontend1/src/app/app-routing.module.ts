import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './components/auth/login/login.component';
import { MainLayoutComponent } from './components/layout/main-layout/main-layout.component'; // Import Layout
import { AdminDashboardComponent } from './components/dashboards/admin-dashboard/admin-dashboard.component';
import { DoctorDashboardComponent } from './components/dashboards/doctor-dashboard/doctor-dashboard.component';
import { SecretaryDashboardComponent } from './components/dashboards/secretary-dashboard/secretary-dashboard.component';
import { roleGuard } from './guards/role.guard'; // Import the role guard

import { AppointmentCalendarComponent } from './components/appointments/appointment-calendar/appointment-calendar.component';
import { PatientListComponent } from './components/patients/patient-list/patient-list.component';
import { PatientFormComponent } from './components/patients/patient-form/patient-form.component';
import { PrescriptionListComponent } from './components/prescriptions/prescription-list/prescription-list.component';
import { PrescriptionFormComponent } from './components/prescriptions/prescription-form/prescription-form.component';
import { InvoiceListComponent } from './components/invoices/invoice-list/invoice-list.component'; // Import List
import { InvoiceFormComponent } from './components/invoices/invoice-form/invoice-form.component';
import { ChatComponent } from './components/chat/chat/chat.component';
import { UserListComponent } from './components/admin/user-list/user-list.component';
import { UserFormComponent } from './components/admin/user-form/user-form.component';
import { AppointmentFormComponent } from './components/appointments/appointment-form/appointment-form.component'; // Import Appointment Form
// Placeholder components - we will create these later
// import { PatientListComponent } from './components/patients/patient-list/patient-list.component';
// import { PatientFormComponent } from './components/patients/patient-form/patient-form.component';
// import { ChatComponent } from './components/chat/chat.component';
import { authGuard } from './guards/auth.guard'; // Import the functional guard

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  // { path: 'login', component: LoginComponent },

  // --- Main Application Routes (Protected by AuthGuard) ---
   {
     path: '', // Represents the root path after login
     component: MainLayoutComponent, // Use MainLayoutComponent as the wrapper
     canActivate: [authGuard], // Protect layout and its children
     children: [
       // { path: '', redirectTo: 'dashboard', pathMatch: 'full' }, // Remove default redirect for now
       {
         path: 'admin-dashboard',
         component: AdminDashboardComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN'] }
       },
       {
         path: 'doctor-dashboard',
         component: DoctorDashboardComponent,
         canActivate: [roleGuard],
         data: { roles: ['DOCTOR'] }
       },
       {
         path: 'secretary-dashboard',
         component: SecretaryDashboardComponent,
         canActivate: [roleGuard],
         data: { roles: ['SECRETARY'] }
       },
       {
         path: 'appointments', // Route for the calendar
         component: AppointmentCalendarComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN', 'DOCTOR', 'SECRETARY'] }
       },
       {
         path: 'appointments/new', // Route for creating a new appointment
         component: AppointmentFormComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN', 'SECRETARY'] } // Only Admin/Secretary can create/edit appointments generally
       },
       {
         path: 'appointments/edit/:id', // Route for editing an existing appointment
         component: AppointmentFormComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN', 'SECRETARY'] }
       },
       {
         path: 'patients', // Route for patient list
         component: PatientListComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN', 'DOCTOR', 'SECRETARY'] }
       },
       {
         path: 'patients/new', // Route for creating a new patient
         component: PatientFormComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN', 'SECRETARY'] } // Only Admin/Secretary can create
       },
       {
         path: 'patients/edit/:id', // Route for editing an existing patient
         component: PatientFormComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN', 'SECRETARY'] }
       },
       {
         path: 'prescriptions', // Route for prescription list
         component: PrescriptionListComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN', 'DOCTOR', 'SECRETARY'] } // All roles can view list
       },
       {
         path: 'prescriptions/new', // Route for creating a new prescription
         component: PrescriptionFormComponent,
         canActivate: [roleGuard],
         data: { roles: ['DOCTOR'] } // Only Doctors can create
       },
       // Optional: Route for creating prescription specific to a patient
       {
         path: 'patients/:patientId/prescriptions/new',
         component: PrescriptionFormComponent,
         canActivate: [roleGuard],
         data: { roles: ['DOCTOR'] }
       },
       // Edit route might not be needed or have stricter rules
       // {
       //   path: 'prescriptions/edit/:id',
       //   component: PrescriptionFormComponent,
       //   canActivate: [roleGuard],
       //   data: { roles: ['DOCTOR'] } // Or maybe only the original doctor?
       // },
       {
         path: 'invoices', // Route for invoice list
         component: InvoiceListComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN', 'SECRETARY'] } // Only Admin/Secretary can manage invoices
       },
       {
         path: 'invoices/new', // Route for creating a new invoice
         component: InvoiceFormComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN', 'SECRETARY'] }
       },
       {
         path: 'chat', // Route for internal chat
         component: ChatComponent,
         canActivate: [roleGuard],
         data: { roles: ['DOCTOR', 'SECRETARY'] } // Only Doctors/Secretaries can chat
       },
       {
         path: 'invoices/edit/:id', // Route for editing an existing invoice
         component: InvoiceFormComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN', 'SECRETARY'] }
       },
       // --- Admin User Management Routes ---
       {
         path: 'admin/users', // Route for user list
         component: UserListComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN'] }
       },
       {
         path: 'admin/users/new', // Route for creating a new user
         component: UserFormComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN'] }
       },
       {
         path: 'admin/users/edit/:id', // Route for editing an existing user
         component: UserFormComponent,
         canActivate: [roleGuard],
         data: { roles: ['ADMIN'] }
       },
      // { path: 'patients', component: PatientListComponent, data: { roles: ['DOCTOR', 'SECRETARY', 'ADMIN'] } },
      // { path: 'patients/new', component: PatientFormComponent, data: { roles: ['SECRETARY', 'ADMIN'] } },
      // { path: 'patients/edit/:id', component: PatientFormComponent, data: { roles: ['SECRETARY', 'ADMIN'] } },

      // { path: 'appointments', component: AppointmentCalendarComponent, data: { roles: ['DOCTOR', 'SECRETARY', 'ADMIN'] } },

      // { path: 'chat', component: ChatComponent, data: { roles: ['DOCTOR', 'SECRETARY'] } },

      // Add routes for prescriptions, invoices, user management etc.
     ]
   },
  // Fallback route for unknown paths
  // { path: '**', redirectTo: '/dashboard' } // Or a dedicated NotFoundComponent
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }