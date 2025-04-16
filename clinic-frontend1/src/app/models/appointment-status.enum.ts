// Matches com.clinicapp.backend.model.core.AppointmentStatus
export enum AppointmentStatus {
    SCHEDULED = 'SCHEDULED',
    CONFIRMED = 'CONFIRMED', // If used
    CHECKED_IN = 'CHECKED_IN', // If used
    COMPLETED = 'COMPLETED',
    CANCELLED = 'CANCELLED',
    NO_SHOW = 'NO_SHOW'
}

// Also create a list for easy iteration in templates if needed
export const AppointmentStatusList = Object.values(AppointmentStatus);