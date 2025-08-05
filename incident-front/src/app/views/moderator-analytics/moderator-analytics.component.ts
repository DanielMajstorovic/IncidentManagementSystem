import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AnalyticsResult, AnalyticsService } from '../../core/services/analytics.service';

interface CalendarDay {
  date: Date;
  dayOfMonth: number;
  isCurrentMonth: boolean;
  isToday: boolean;
  incidentCount: number;
}

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe],
  templateUrl: './moderator-analytics.component.html',
  styleUrls: ['./moderator-analytics.component.scss']
})
export class ModeratorAnalyticsComponent implements OnInit, OnDestroy {
  // Loading and messaging
  loading = false;
  error = '';

  // Data stores
  incidentsByType: AnalyticsResult[] = [];
  incidentsByCity: AnalyticsResult[] = [];
  private incidentsByDayMap = new Map<string, number>();

  // Calendar properties
  currentDate: Date = new Date();
  calendarDays: CalendarDay[] = [];
  weekdays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  private destroy$ = new Subject<void>();

  constructor(private analyticsService: AnalyticsService) {}

  ngOnInit(): void {
    this.loadAllData();
    this.analyticsService.loading$.pipe(takeUntil(this.destroy$)).subscribe((loading: any) => this.loading = loading);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadAllData(): void {
    this.error = '';
    forkJoin({
      byType: this.analyticsService.getIncidentsByType(),
      byDay: this.analyticsService.getIncidentsByDay(),
      byCity: this.analyticsService.getIncidentsByCity()
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: ({ byType, byDay, byCity }) => {
        this.incidentsByType = byType;
        this.incidentsByCity = byCity;
        
        // Kreiramo mapu za brzi pristup broju incidenata po datumu
        this.incidentsByDayMap.clear();
        byDay.forEach((result: any) => this.incidentsByDayMap.set(result.key, result.count));
        
        this.generateCalendar();
      },
      error: err => {
        this.error = 'Failed to load analytics data. Please try again.';
        console.error(err);
      }
    });
  }

  generateCalendar(): void {
    this.calendarDays = [];
    const year = this.currentDate.getFullYear();
    const month = this.currentDate.getMonth();

    const firstDayOfMonth = new Date(year, month, 1);
    const lastDayOfMonth = new Date(year, month + 1, 0);
    const firstDayOfWeek = firstDayOfMonth.getDay(); // 0=Sunday, 1=Monday...
    const totalDays = lastDayOfMonth.getDate();

    // Dodaj prazne dane za početak meseca
    for (let i = 0; i < firstDayOfWeek; i++) {
      this.calendarDays.push({} as CalendarDay);
    }

    // Dodaj dane u mesecu
    for (let day = 1; day <= totalDays; day++) {
      const date = new Date(year, month, day);
      const dateString = this.formatDate(date);
      const today = new Date();

      this.calendarDays.push({
        date: date,
        dayOfMonth: day,
        isCurrentMonth: true,
        isToday: date.setHours(0, 0, 0, 0) === today.setHours(0, 0, 0, 0),
        incidentCount: this.incidentsByDayMap.get(dateString) || 0
      });
    }
  }

  changeMonth(offset: number): void {
    this.currentDate.setMonth(this.currentDate.getMonth() + offset);
    this.currentDate = new Date(this.currentDate); // Force change detection
    this.generateCalendar();
  }

  // Helper funkcija za formatiranje datuma u YYYY-MM-DD
  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const day = date.getDate().toString().padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  // Helper za bar chart
  getBarWidth(count: number, data: AnalyticsResult[]): number {
    const maxCount = Math.max(...data.map(item => item.count), 1);
    return (count / maxCount) * 100;
  }

  // Get total incidents for summary
  getTotalIncidents(): number {
    return this.incidentsByType.reduce((sum, item) => sum + item.count, 0);
  }

  // Get most active city
  getMostActiveCity(): string {
    if (this.incidentsByCity.length === 0) return 'N/A';
    const topCity = this.incidentsByCity.reduce((prev, current) => 
      prev.count > current.count ? prev : current
    );
    return topCity.key || 'Unknown';
  }

  // Get most common incident type
  getMostCommonType(): string {
    if (this.incidentsByType.length === 0) return 'N/A';
    const topType = this.incidentsByType.reduce((prev, current) => 
      prev.count > current.count ? prev : current
    );
    return topType.key;
  }

  // TrackBy function for better performance with *ngFor
  trackByKey(index: number, item: AnalyticsResult): string {
    return item.key;
  }
}