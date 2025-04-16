import { Component, OnInit, ViewChild, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FullCalendarComponent, FullCalendarModule } from '@fullcalendar/angular'; // Import FullCalendar modules
import { CalendarOptions, EventInput, EventClickArg, DateSelectArg } from '@fullcalendar/core'; // Import types
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import listPlugin from '@fullcalendar/list';
import interactionPlugin from '@fullcalendar/interaction'; // For dateClick, eventClick, select, etc.

import { AppointmentService } from '../../../services/core/appointment.service'; // Import the service
import { AppointmentDTO } from '../../../models/appointment.dto'; // Import the DTO
// import { AppointmentDTO } from '../../../models/appointment.dto'; // Placeholder DTO

@Component({
  selector: 'app-appointment-calendar',
  standalone: true,
  imports: [CommonModule, FullCalendarModule], // Add FullCalendarModule to imports
  templateUrl: './appointment-calendar.component.html',
  styleUrls: ['./appointment-calendar.component.css']
})
export class AppointmentCalendarComponent implements OnInit {

  @ViewChild('calendar') calendarComponent!: FullCalendarComponent; // Access the component instance

  calendarOptions: CalendarOptions = {
    plugins: [dayGridPlugin, timeGridPlugin, listPlugin, interactionPlugin],
    initialView: 'timeGridWeek', // Default view
    headerToolbar: {
      left: 'prev,next today',
      center: 'title',
      right: 'dayGridMonth,timeGridWeek,timeGridDay,listWeek' // View options
    },
    weekends: true, // Show weekends
    editable: true, // Allow event dragging & resizing
    selectable: true, // Allow date selection
    selectMirror: true,
    dayMaxEvents: true, // Allow "more" link when too many events
    events: this.loadAppointments.bind(this), // Function to load events
    select: this.handleDateSelect.bind(this), // Callback for date selection
    eventClick: this.handleEventClick.bind(this), // Callback for event click
    // eventDrop: this.handleEventDrop.bind(this), // Callback for event drag-n-drop
    // eventResize: this.handleEventResize.bind(this), // Callback for event resize
    // Add more options as needed (e.g., businessHours, slotDuration, locale)
    // businessHours: { // Example: Define working hours
    //   daysOfWeek: [ 1, 2, 3, 4, 5 ], // Monday - Friday
    //   startTime: '09:00',
    //   endTime: '18:00',
    // }
  };

  // Placeholder for appointments data
  currentEvents: EventInput[] = [];

  constructor(
    private changeDetector: ChangeDetectorRef,
    private appointmentService: AppointmentService // Inject the service
  ) {}

  ngOnInit(): void {
    // Load initial appointments (or trigger the 'events' function)
  }

  // --- Event Loading ---
  loadAppointments(fetchInfo: any, successCallback: (events: EventInput[]) => void, failureCallback: (error: any) => void): void {
    console.log('Fetching appointments for:', fetchInfo.start, 'to', fetchInfo.end);

    this.appointmentService.getAppointmentsInRange(fetchInfo.start, fetchInfo.end).subscribe({
      next: (appointments: AppointmentDTO[]) => {
        const events = appointments.map(app => ({
          id: String(app.id),
          title: `Appt: ${app.patientFirstName} ${app.patientLastName} (Dr. ${app.doctorLastName})`, // Construct a meaningful title
          start: app.startTime, // Backend should provide ISO string
          end: app.endTime,     // Backend should provide ISO string
          // Example: Add color based on status
          backgroundColor: this.getEventColor(app.status),
          borderColor: this.getEventColor(app.status),
          extendedProps: { // Store original DTO data if needed for eventClick
            appointmentData: app
          }
        }));
        successCallback(events);
        this.currentEvents = events; // Update local copy
        this.changeDetector.detectChanges(); // Trigger change detection as data comes async
      },
      error: (error) => {
        console.error('Error fetching appointments:', error);
        // Optionally display an error message to the user
        failureCallback(error); // Inform FullCalendar about the failure
      }
    });
  }

  // --- Calendar Interaction Handlers ---

  handleDateSelect(selectInfo: DateSelectArg) {
    // TODO: Implement logic to open an appointment creation modal/form
    console.log('Date selected:', selectInfo);
    alert(`Date selected: Start=${selectInfo.startStr}, End=${selectInfo.endStr}, AllDay=${selectInfo.allDay}`);

    // Example: Open a modal or navigate to a form
    // this.openAppointmentModal({ start: selectInfo.start, end: selectInfo.end, allDay: selectInfo.allDay });

    // Unselect the date range visually
    const calendarApi = selectInfo.view.calendar;
    calendarApi.unselect();
  }

  handleEventClick(clickInfo: EventClickArg) {
    // TODO: Implement logic to view/edit the clicked appointment
    console.log('Event clicked:', clickInfo.event);
    alert(`Event clicked: ${clickInfo.event.title}`);

    // Example: Open a modal with appointment details
    // const appointmentData = clickInfo.event.extendedProps['appointmentData'];
    // this.openAppointmentModal(appointmentData);
  }

  // TODO: Implement handleEventDrop and handleEventResize if needed
  // handleEventDrop(dropInfo: EventDropArg) { ... }
  // handleEventResize(resizeInfo: EventResizeArg) { ... }

  // TODO: Add method to refresh calendar events after create/update/delete
  refreshCalendarEvents() {
     if (this.calendarComponent) {
       this.calendarComponent.getApi().refetchEvents();
     }
  }

 // Helper to get color based on status (example)
 getEventColor(status: AppointmentDTO['status']): string {
   switch (status) {
     case 'SCHEDULED': return '#3788d8'; // Blue
     case 'CONFIRMED': return '#28a745'; // Green (Example)
     case 'CANCELLED': return '#dc3545'; // Red
     case 'COMPLETED': return '#6c757d'; // Gray
     case 'NO_SHOW': return '#ffc107'; // Yellow (Example)
     default: return '#3788d8'; // Default blue
   }
 }
}
