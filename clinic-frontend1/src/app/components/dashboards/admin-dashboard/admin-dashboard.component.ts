import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NgxChartsModule, ScaleType, Color } from '@swimlane/ngx-charts'; // Import Color interface
import { DashboardService, AdminDashboardStats, RevenueData, AppointmentCountData } from '../../../services/dashboard/dashboard.service';
import { Observable, forkJoin, map } from 'rxjs'; // Import forkJoin and map

// Interface for chart data format expected by ngx-charts
interface ChartDataPoint {
  name: string;
  value: number;
}

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NgxChartsModule], // Add NgxChartsModule
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {

  isLoading = true;
  errorMessage: string | null = null;

  // Data properties
  stats: AdminDashboardStats | null = null;
  revenueChartData: ChartDataPoint[] = [];
  appointmentsChartData: ChartDataPoint[] = [];

  // Chart options (customize as needed)
  revenueView: [number, number] = [700, 300]; // width, height
  revenueColorScheme: Color = { // Define type as Color
    name: 'revenueScheme', // Add name
    selectable: true, // Add selectable
    group: ScaleType.Ordinal, // Add group (Ordinal for categorical)
    domain: ['#5AA454', '#A10A28', '#C7B42C', '#AAAAAA'] // Keep domain
  };
  revenueGradient = false;
  revenueShowXAxis = true;
  revenueShowYAxis = true;
  revenueShowXAxisLabel = true;
  revenueXAxisLabel = 'Month';
  revenueShowYAxisLabel = true;
  revenueYAxisLabel = 'Revenue ($)';

  appointmentsView: [number, number] = [700, 300];
  appointmentsColorScheme: Color = { // Define type as Color
    name: 'appointmentScheme', // Add name
    selectable: true, // Add selectable
    group: ScaleType.Ordinal, // Add group
    domain: ['#3B82F6', '#10B981', '#F59E0B', '#EF4444'] // Keep domain
  };
  appointmentsGradient = false;
  appointmentsShowXAxis = true;
  appointmentsShowYAxis = true;
  appointmentsShowXAxisLabel = true;
  appointmentsXAxisLabel = 'Day of Week';
  appointmentsShowYAxisLabel = true;
  appointmentsYAxisLabel = 'Appointments';
  appointmentsAutoScale = true;


  constructor(private dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // Use forkJoin to fetch all data concurrently
    forkJoin({
      stats: this.dashboardService.getAdminStats(),
      revenue: this.dashboardService.getAdminRevenueOverview(), // Defaults to current year
      appointments: this.dashboardService.getAdminWeeklyAppointments() // Defaults to current week
    }).pipe(
      map(({ stats, revenue, appointments }) => {
        // Transform data for charts
        const revenueChartData = this.transformObjectToChartData(revenue);
        const appointmentsChartData = this.transformObjectToChartData(appointments);
        return { stats, revenueChartData, appointmentsChartData };
      })
    ).subscribe({
      next: (data) => {
        this.stats = data.stats;
        this.revenueChartData = data.revenueChartData;
        this.appointmentsChartData = data.appointmentsChartData;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error loading admin dashboard data:', err);
        this.errorMessage = 'Failed to load dashboard data. Please try again.';
        // Handle specific errors like 403 Forbidden if necessary
        if (err.status === 403) {
           this.errorMessage = 'Access Denied: You do not have permission to view dashboard data.';
        }
        this.isLoading = false;
      }
    });
  }

  // Helper function to convert { key: value } object to ngx-charts format [{ name: key, value: value }]
  private transformObjectToChartData(data: RevenueData | AppointmentCountData): ChartDataPoint[] {
    return Object.entries(data).map(([name, value]) => ({ name, value }));
  }

  // Optional: Add methods for chart interactions (e.g., onSelect)
  onChartSelect(event: any): void {
    console.log('Chart event:', event);
  }
}
