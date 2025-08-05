import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AlertService, AlertSettingsDto, LastAlertInfoDto } from '../../core/services/alert.service';

@Component({
  selector: 'app-alert-params',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DatePipe],
  templateUrl: './alert-params.component.html',
  styleUrls: ['./alert-params.component.scss']
})
export class AlertParamsComponent implements OnInit, OnDestroy {
  // Form and data
  settingsForm: FormGroup;
  lastAlertInfo: LastAlertInfoDto | null = null;
  timeSinceLastAlert = '';

  // State management
  loading = false;
  saving = false;
  error = '';
  successMessage = '';
  
  private destroy$ = new Subject<void>();
  private timerInterval: any;

  constructor(
    private alertService: AlertService,
    private fb: FormBuilder
  ) {
    this.settingsForm = this.fb.group({
      timeWindowDays: [7, [Validators.required, Validators.min(1), Validators.max(30)]],
      minIncidentsThreshold: [5, [Validators.required, Validators.min(1), Validators.max(50)]],
      maxRadiusMeters: [500, [Validators.required, Validators.min(50), Validators.max(5000)]],
      cooldownPeriodMinutes: [60, [Validators.required, Validators.min(1), Validators.max(1440)]]
    });
  }

  ngOnInit(): void {
    this.loadInitialData();
    this.alertService.loading$.pipe(takeUntil(this.destroy$)).subscribe((loading: any) => this.loading = loading);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
  }

  loadInitialData(): void {
    this.error = '';
    forkJoin({
      settings: this.alertService.getAlertSettings(),
      lastAlert: this.alertService.getLastAlertInfo()
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: ({ settings, lastAlert }) => {
        this.settingsForm.patchValue(settings);
        this.lastAlertInfo = lastAlert;
        this.startTimeAgoTimer();
      },
      error: err => {
        this.error = 'Failed to load initial settings. Please try again.';
        console.error(err);
      }
    });
  }
  
  onSubmit(): void {
    if (this.settingsForm.invalid) {
      this.error = 'Please correct the errors in the form.';
      this.settingsForm.markAllAsTouched();
      this.autoHideMessage();
      return;
    }

    this.saving = true;
    this.error = '';
    this.successMessage = '';

    const settings: AlertSettingsDto = this.settingsForm.value;

    this.alertService.updateAlertSettings(settings)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedSettings: any) => {
          this.settingsForm.patchValue(updatedSettings);
          this.settingsForm.markAsPristine();
          this.successMessage = 'Alert settings updated successfully!';
          this.saving = false;
          this.autoHideMessage();
        },
        error: (err: any) => {
          this.error = 'Failed to save settings. Please try again.';
          this.saving = false;
          console.error(err);
          this.autoHideMessage();
        }
      });
  }
  
  startTimeAgoTimer(): void {
    if (this.timerInterval) {
      clearInterval(this.timerInterval);
    }
    this.updateTimeAgo();
    this.timerInterval = setInterval(() => this.updateTimeAgo(), 60000);
  }
  
  updateTimeAgo(): void {
    if (!this.lastAlertInfo?.lastAlertTimestamp) {
      this.timeSinceLastAlert = 'No alerts sent recently';
      return;
    }

    const lastAlertDate = new Date(this.lastAlertInfo.lastAlertTimestamp);
    const now = new Date();
    const seconds = Math.floor((now.getTime() - lastAlertDate.getTime()) / 1000);

    let interval = seconds / 31536000;
    if (interval > 1) { this.timeSinceLastAlert = Math.floor(interval) + " years ago"; return; }
    interval = seconds / 2592000;
    if (interval > 1) { this.timeSinceLastAlert = Math.floor(interval) + " months ago"; return; }
    interval = seconds / 86400;
    if (interval > 1) { this.timeSinceLastAlert = Math.floor(interval) + " days ago"; return; }
    interval = seconds / 3600;
    if (interval > 1) { this.timeSinceLastAlert = Math.floor(interval) + " hours ago"; return; }
    interval = seconds / 60;
    if (interval > 1) { this.timeSinceLastAlert = Math.floor(interval) + " minutes ago"; return; }
    this.timeSinceLastAlert = "Just now";
  }

  private autoHideMessage(): void {
    setTimeout(() => {
      this.error = '';
      this.successMessage = '';
    }, 5000);
  }

  // Helper methods for form validation display
  isFieldInvalid(fieldName: string): boolean {
    const field = this.settingsForm.get(fieldName);
    return !!(field && field.invalid && field.touched);
  }

  getFieldError(fieldName: string): string {
    const field = this.settingsForm.get(fieldName);
    if (field && field.errors && field.touched) {
      if (field.errors['required']) return 'This field is required';
      if (field.errors['min']) return `Minimum value is ${field.errors['min'].min}`;
      if (field.errors['max']) return `Maximum value is ${field.errors['max'].max}`;
    }
    return '';
  }
}