import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

export interface AlertSettingsDto {
  timeWindowDays: number;
  minIncidentsThreshold: number;
  maxRadiusMeters: number;
  cooldownPeriodMinutes: number;
}

export interface LastAlertInfoDto {
  lastAlertTimestamp: string | null; // Tip ostaje isti
}

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  // Ažuriramo baznu putanju da odgovara kontroleru
  private settingsApiUrl = '/alert-service/api/v1/alerts/settings';
  private lastAlertApiUrl = '/alert-service/api/v1/alerts/last';

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {}

  private setLoading(isLoading: boolean): void {
    this.loadingSubject.next(isLoading);
  }

  getAlertSettings(): Observable<AlertSettingsDto> {
    this.setLoading(true);
    // Koristimo novu, precizniju putanju
    return this.http.get<AlertSettingsDto>(this.settingsApiUrl).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        console.error("Failed to get alert settings:", error);
        throw error;
      })
    );
  }

  updateAlertSettings(settings: AlertSettingsDto): Observable<AlertSettingsDto> {
    this.setLoading(true);
    // Koristimo novu, precizniju putanju
    return this.http.put<AlertSettingsDto>(this.settingsApiUrl, settings).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        console.error("Failed to update alert settings:", error);
        throw error;
      })
    );
  }

  // ===== KLJUČNA IZMENA: Više nije simulacija! =====
  getLastAlertInfo(): Observable<LastAlertInfoDto> {
    // Pozivamo pravi backend endpoint
    return this.http.get<LastAlertInfoDto>(this.lastAlertApiUrl).pipe(
      catchError(error => {
        console.error("Failed to get last alert info:", error);
        throw error;
      })
    );
  }
}