import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

// DTO koji odgovara onome što vraća backend
export interface AnalyticsResult {
  key: string;
  count: number;
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private apiUrl = '/analytics-service/api/v1/analytics';

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {}

  private setLoading(isLoading: boolean): void {
    this.loadingSubject.next(isLoading);
  }

  getIncidentsByType(): Observable<AnalyticsResult[]> {
    this.setLoading(true);
    return this.http.get<AnalyticsResult[]>(`${this.apiUrl}/by-type`).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        console.error("Failed to get incidents by type:", error);
        throw error;
      })
    );
  }

  getIncidentsByDay(): Observable<AnalyticsResult[]> {
    this.setLoading(true);
    return this.http.get<AnalyticsResult[]>(`${this.apiUrl}/by-day`).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        console.error("Failed to get incidents by day:", error);
        throw error;
      })
    );
  }

  getIncidentsByCity(): Observable<AnalyticsResult[]> {
    this.setLoading(true);
    return this.http.get<AnalyticsResult[]>(`${this.apiUrl}/by-city`).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        console.error("Failed to get incidents by city:", error);
        throw error;
      })
    );
  }
}