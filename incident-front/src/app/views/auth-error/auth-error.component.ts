import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';

interface ErrorInfo {
  title: string;
  description: string;
  icon: string;
  color: string;
}

@Component({
  selector: 'app-auth-error',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="auth-error-container">
      <div class="error-backdrop">
        <div class="error-card">
          <div class="error-header" [ngClass]="'bg-' + errorInfo.color">
            <div class="error-icon">
              <i class="bi" [ngClass]="'bi-' + errorInfo.icon"></i>
            </div>
            <h1 class="error-title">{{ errorInfo.title }}</h1>
          </div>
          
          <div class="error-body">
            <p class="error-description">{{ errorInfo.description }}</p>
            
            <div class="error-details" *ngIf="errorType">
              <div class="detail-item">
                <span class="detail-label">Error Code:</span>
                <span class="detail-value">{{ errorType | uppercase }}</span>
              </div>
              <div class="detail-item" *ngIf="timestamp">
                <span class="detail-label">Time:</span>
                <span class="detail-value">{{ timestamp | date:'medium' }}</span>
              </div>
            </div>
            
            <div class="error-actions">
              <button class="btn btn-primary btn-lg" (click)="goHome()">
                <i class="bi bi-house-fill me-2"></i>
                Return to Home
              </button>
              
            </div>
          </div>
          
          <div class="error-footer">
            <p class="help-text">
              <i class="bi bi-info-circle me-1"></i>
              If this problem persists, please contact system administrator.
            </p>
          </div>
        </div>
      </div>
      
      <!-- Animated background elements -->
      <div class="bg-animation">
        <div class="floating-shape shape-1"></div>
        <div class="floating-shape shape-2"></div>
        <div class="floating-shape shape-3"></div>
        <div class="floating-shape shape-4"></div>
      </div>
    </div>
  `,
  styleUrls: ['./auth-error.component.scss']
})
export class AuthErrorComponent implements OnInit {
  errorType: string = '';
  errorInfo: ErrorInfo = {
    title: 'Authentication Error',
    description: 'An unexpected error occurred during authentication.',
    icon: 'exclamation-triangle-fill',
    color: 'warning'
  };
  timestamp: Date = new Date();

  private errorMapping: { [key: string]: ErrorInfo } = {
    'invalid_domain': {
      title: 'Invalid Domain',
      description: 'Your email domain is not authorized to access this system. Please contact your administrator to request domain approval.',
      icon: 'shield-exclamation',
      color: 'danger'
    },
    'user_blocked': {
      title: 'Account Blocked',
      description: 'Your account has been temporarily blocked. This may be due to security reasons or policy violations. Please contact support for assistance.',
      icon: 'person-fill-slash',
      color: 'danger'
    },
    'login_failed': {
      title: 'Login Failed',
      description: 'Authentication failed. Please verify your information and try again.',
      icon: 'key-fill',
      color: 'warning'
    }
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.errorType = params['error'] || 'unknown';
      
      if (this.errorMapping[this.errorType]) {
        this.errorInfo = this.errorMapping[this.errorType];
      } else {
        // Default error for unknown types
        this.errorInfo = {
          title: 'Authentication Error',
          description: 'An unexpected error occurred during the authentication process. Please try again later.',
          icon: 'exclamation-triangle-fill',
          color: 'warning'
        };
      }
    });
  }

  goHome(): void {
    this.router.navigate(['/']);
  }
}