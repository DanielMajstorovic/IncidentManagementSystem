import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, TitleCasePipe, DatePipe } from '@angular/common';
import * as L from 'leaflet';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { Subject, BehaviorSubject } from 'rxjs';
import { debounceTime, takeUntil, finalize } from 'rxjs/operators';
import { IncidentService } from '../../core/services/incident.service';
import { FilterRequest, Incident } from '../../core/models/incident.model';
import { ReplacePipe } from '../../core/pipes/replace.pipe';

declare var bootstrap: any;

const iconColorMap: { [key: string]: string } = {
  FIRE: 'red',
  FLOOD: 'blue',
  ACCIDENT: 'orange',
  CRIME: 'black',
  DEFAULT: 'grey',
};

// Special colors for moderation states
const moderationIconColorMap: { [key: string]: string } = {
  REPORTED: 'yellow',
  PENDING: 'violet',
  ...iconColorMap
};

const createColoredIcon = (color: string): L.Icon => {
  return L.icon({
    iconUrl: `https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-${color}.png`,
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41],
  });
};

interface ModalState {
  isOpen: boolean;
  isLoading: boolean;
  incident: Incident | null;
  imageUrls: string[];
  currentImageIndex: number;
  error: string | null;
  isProcessing: boolean;
}

@Component({
  selector: 'app-moderator-incidents',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    LeafletModule,
    ReplacePipe,
    TitleCasePipe,
    DatePipe,
  ],
  templateUrl: './moderator-incidents.component.html',
  styleUrls: ['./moderator-incidents.component.scss'],
})
export class ModeratorIncidentsComponent implements OnInit, OnDestroy {
  map!: L.Map;
  mapOptions: L.MapOptions = {
    layers: [
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap',
        className: 'map-tiles',
      }),
    ],
    zoom: 7,
    center: L.latLng(44.2, 17.8),
  };

  incidentLayers: L.Layer[] = [];
  filterForm: FormGroup;
  incidentTypes = ['FIRE', 'FLOOD', 'ACCIDENT', 'CRIME'];
  incidentSubtypes: { [key: string]: string[] } = {
    FIRE: ['BUILDING_FIRE'],
    ACCIDENT: ['CAR_ACCIDENT'],
    CRIME: ['ROBBERY', 'ASSAULT'],
  };
  currentSubtypes: string[] = [];

  // Moderation specific statuses
  moderationStatuses = [
    { value: 'REPORTED', label: 'Reported', color: 'yellow', icon: 'exclamation-triangle' },
    { value: 'PENDING', label: 'Pending Review', color: 'violet', icon: 'clock' },
  ];

  timeRanges = [
    { value: '1h', label: 'Last Hour', icon: 'clock' },
    { value: '24h', label: 'Last 24H', icon: 'clock-history' },
    { value: '7d', label: 'Last 7D', icon: 'calendar-week' },
    { value: '31d', label: 'Last Month', icon: 'calendar-month' },
  ];

  // Enhanced legend for moderation
  legendData = [
    ...this.moderationStatuses.map(status => ({
      type: status.label,
      color: status.color,
      icon: status.icon,
      status: status.value
    })),
    ...Object.keys(iconColorMap)
      .filter((key) => key !== 'DEFAULT')
      .map((key) => ({
        type: key,
        color: iconColorMap[key],
        icon: this.getTypeIcon(key),
      }))
  ];

  // Enhanced modal state for moderation
  private modalStateSubject = new BehaviorSubject<ModalState>({
    isOpen: false,
    isLoading: false,
    incident: null,
    imageUrls: [],
    currentImageIndex: 0,
    error: null,
    isProcessing: false,
  });

  public modalState$ = this.modalStateSubject.asObservable();
  public currentModalState: ModalState = this.modalStateSubject.value;

  // Loading and UI states
  public isMapLoading = true;
  public isFilterCollapsed = false;
  public activeFiltersCount = 0;
  public totalIncidents = 0;
  public pendingCount = 0;
  public reportedCount = 0;

  private incidentModal: any;
  private destroy$ = new Subject<void>();

  constructor(
    private incidentService: IncidentService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    this.filterForm = this.fb.group({
      incidentType: [''],
      incidentSubtype: [''],
      location: [''],
      timeRange: [''],
      status: [''], // Added status filter for moderation
    });

    // Subscribe to modal state changes
    this.modalState$.pipe(takeUntil(this.destroy$)).subscribe((state) => {
      this.currentModalState = state;
      this.cdr.detectChanges();
    });
  }

  ngOnInit(): void {
    this.initializeModal();
    this.loadIncidents();
    this.setupFormSubscription();
    this.updateActiveFiltersCount();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private setupFormSubscription(): void {
    this.filterForm.valueChanges
      .pipe(
        debounceTime(200),
        takeUntil(this.destroy$)
      )
      .subscribe(() => {
        this.loadIncidents();
        this.updateActiveFiltersCount();
      });
  }

  private updateActiveFiltersCount(): void {
    const values = this.filterForm.value;
    this.activeFiltersCount = Object.values(values).filter(
      (v) => v && v !== ''
    ).length;
  }

  onMapReady(map: L.Map): void {
    this.map = map;
    this.isMapLoading = false;
    this.addMapControls();
  }

  private addMapControls(): void {
    const zoomControl = L.control.zoom({ position: 'topright' });
    this.map.addControl(zoomControl);
  }

  onTypeChange(): void {
    const selectedType = this.filterForm.get('incidentType')?.value;
    this.currentSubtypes = this.incidentSubtypes[selectedType || ''] || [];
    this.filterForm.get('incidentSubtype')?.setValue('');
  }

  resetFilters(): void {
    this.filterForm.reset({
      incidentType: '',
      incidentSubtype: '',
      location: '',
      timeRange: '',
      status: '',
    });
  }

  toggleFilterPanel(): void {
    this.isFilterCollapsed = !this.isFilterCollapsed;
  }

  // Enhanced modal opening for moderation
  openIncidentModal(incident: Incident): void {
    this.updateModalState({
      isOpen: true,
      isLoading: true,
      incident: incident,
      imageUrls: [],
      currentImageIndex: 0,
      error: null,
      isProcessing: false,
    });

    this.incidentModal?.show();

    // Preload images
    if (incident.images && incident.images.length > 0) {
      const imageFilenames = incident.images.map((img) => img.imageUrl);
      this.incidentService
        .preloadImages(imageFilenames)
        .pipe(
          finalize(() => {
            this.updateModalState({
              ...this.currentModalState,
              isLoading: false,
            });
          })
        )
        .subscribe({
          next: (urls) => {
            this.updateModalState({
              ...this.currentModalState,
              imageUrls: urls,
              isLoading: false,
            });
          },
          error: (error) => {
            console.error('Failed to load images:', error);
            this.updateModalState({
              ...this.currentModalState,
              isLoading: false,
              error: 'Failed to load images',
            });
          },
        });
    } else {
      this.updateModalState({
        ...this.currentModalState,
        isLoading: false,
      });
    }
  }

  private updateModalState(newState: Partial<ModalState>): void {
    const currentState = this.modalStateSubject.value;
    this.modalStateSubject.next({ ...currentState, ...newState });
  }

  // Image navigation methods
  previousImage(): void {
    const currentIndex = this.currentModalState.currentImageIndex;
    const newIndex =
      currentIndex > 0
        ? currentIndex - 1
        : this.currentModalState.imageUrls.length - 1;
    this.updateModalState({
      ...this.currentModalState,
      currentImageIndex: newIndex,
    });
  }

  nextImage(): void {
    const currentIndex = this.currentModalState.currentImageIndex;
    const newIndex =
      currentIndex < this.currentModalState.imageUrls.length - 1
        ? currentIndex + 1
        : 0;
    this.updateModalState({
      ...this.currentModalState,
      currentImageIndex: newIndex,
    });
  }

  goToImage(index: number): void {
    this.updateModalState({
      ...this.currentModalState,
      currentImageIndex: index,
    });
  }

  // MODERATION ACTIONS
  approveIncident(): void {
    if (!this.currentModalState.incident) return;

    this.updateModalState({
      ...this.currentModalState,
      isProcessing: true,
    });

    const incidentId = this.currentModalState.incident.id;
    this.incidentService.updateIncidentStatus(incidentId, 'APPROVED').subscribe({
      next: (updatedIncident) => {
        console.log('Incident approved successfully');
        this.updateModalState({
          ...this.currentModalState,
          isProcessing: false,
        });
        this.incidentModal?.hide();
        this.loadIncidents(); // Refresh the map
        this.showToast('Incident approved successfully', 'success');
      },
      error: (error) => {
        console.error('Failed to approve incident:', error);
        this.updateModalState({
          ...this.currentModalState,
          isProcessing: false,
          error: 'Failed to approve incident',
        });
        this.showToast('Failed to approve incident', 'error');
      },
    });
  }

  rejectIncident(): void {
    if (!this.currentModalState.incident) return;

    this.updateModalState({
      ...this.currentModalState,
      isProcessing: true,
    });

    const incidentId = this.currentModalState.incident.id;
    this.incidentService.updateIncidentStatus(incidentId, 'REJECTED').subscribe({
      next: (updatedIncident) => {
        console.log('Incident rejected successfully');
        this.updateModalState({
          ...this.currentModalState,
          isProcessing: false,
        });
        this.incidentModal?.hide();
        this.loadIncidents(); // Refresh the map
        this.showToast('Incident rejected', 'warning');
      },
      error: (error) => {
        console.error('Failed to reject incident:', error);
        this.updateModalState({
          ...this.currentModalState,
          isProcessing: false,
          error: 'Failed to reject incident',
        });
        this.showToast('Failed to reject incident', 'error');
      },
    });
  }

  private loadIncidents(): void {
    const rawFilters = this.filterForm.value;
    const cleanFilters: Partial<FilterRequest> = {};

    Object.keys(rawFilters).forEach((key) => {
      const value = rawFilters[key];
      if (value !== '' && value !== null) {
        (cleanFilters as any)[key] = value;
      }
    });

    // Default to showing incidents that need moderation
    if (!cleanFilters.status) {
      cleanFilters.status = 'REPORTED'; // or 'PENDING'
    }

    const request: FilterRequest = { ...cleanFilters };

    this.incidentService.filterIncidents(request).subscribe({
      next: (response) => {
        this.incidentLayers = [];
        const markerPoints: L.LatLng[] = [];
        
        // Update counters
        this.totalIncidents = response.content.length;
        this.reportedCount = response.content.filter(i => i.status === 'REPORTED').length;
        this.pendingCount = response.content.filter(i => i.status === 'PENDING').length;

        response.content.forEach((incident) => {
          const point = L.latLng(
            incident.location.latitude,
            incident.location.longitude
          );
          markerPoints.push(point);

          // Use moderation colors for pending incidents
          const color = moderationIconColorMap[incident.status] || 
                       moderationIconColorMap[incident.type] || 
                       moderationIconColorMap['DEFAULT'];
          
          const icon = createColoredIcon(color);
          const marker = L.marker(point, { icon });

          // Enhanced tooltip for moderation
          marker.on('mouseover', () => {
            marker
              .bindTooltip(
                `${incident.status} - ${incident.type}: ${incident.description}`,
                {
                  permanent: false,
                  direction: 'bottom',
                }
              )
              .openTooltip();
          });

          marker.on('click', () => this.openIncidentModal(incident));
          this.incidentLayers.push(marker);
        });

        if (this.map && markerPoints.length > 0) {
          const bounds = L.latLngBounds(markerPoints);
          this.map.fitBounds(bounds, { padding: [50, 50] });
        }
      },
      error: (error) => {
        console.error('Failed to load incidents:', error);
        this.showToast('Failed to load incidents', 'error');
      },
    });
  }

  private initializeModal(): void {
    const modalElement = document.getElementById('moderatorIncidentModal');
    if (modalElement) {
      this.incidentModal = new bootstrap.Modal(modalElement);
      modalElement.addEventListener('hidden.bs.modal', () => {
        this.updateModalState({
          isOpen: false,
          isLoading: false,
          incident: null,
          imageUrls: [],
          currentImageIndex: 0,
          error: null,
          isProcessing: false,
        });
      });
    }
  }

  onImageError(event: Event): void {
    const target = event.target as HTMLImageElement;
    target.src = '/assets/image-error.png';
  }

  private getTypeIcon(type: string): string {
    const icons: { [key: string]: string } = {
      FIRE: 'fire',
      FLOOD: 'water',
      ACCIDENT: 'car-front-fill',
      CRIME: 'shield-exclamation',
    };
    return icons[type] || 'exclamation-triangle';
  }

  getFormattedAddress(location: any): string | null {
    const parts: string[] = [];
    if (location.address) parts.push(location.address);
    if (location.city) parts.push(location.city);
    if (location.state) parts.push(location.state);
    if (location.zipcode) parts.push(location.zipcode);
    if (location.country) parts.push(location.country);
    return parts.length > 0 ? parts.join(', ') : null;
  }

  async copyCoordinates(location: any): Promise<void> {
    const coordinates = `${location.latitude}, ${location.longitude}`;
    try {
      await navigator.clipboard.writeText(coordinates);
      this.showToast('Coordinates copied to clipboard', 'success');
    } catch (err) {
      console.error('Failed to copy coordinates:', err);
      // Fallback for older browsers
      const textArea = document.createElement('textarea');
      textArea.value = coordinates;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      this.showToast('Coordinates copied to clipboard', 'success');
    }
  }

  openInMaps(location: any): void {
    const url = `https://www.google.com/maps?q=${location.latitude},${location.longitude}`;
    window.open(url, '_blank');
  }

  centerMapOnLocation(location: any): void {
    if (this.map) {
      const latLng = L.latLng(location.latitude, location.longitude);
      this.map.setView(latLng, 15);
      this.incidentModal?.hide();
    }
  }

  // Quick filter methods for moderation
  showReportedOnly(): void {
    this.filterForm.patchValue({ status: 'REPORTED' });
  }

  showPendingOnly(): void {
    this.filterForm.patchValue({ status: 'PENDING' });
  }

  showAllModeration(): void {
    this.filterForm.patchValue({ status: '' });
  }

  // Toast notification system (you might want to implement a proper toast service)
  private showToast(message: string, type: 'success' | 'error' | 'warning'): void {
    // Simple console log for now - implement proper toast notifications
    console.log(`${type.toUpperCase()}: ${message}`);
    // You can implement a proper toast service here
  }

  get isLoading(): boolean {
    return this.incidentService.loading$ as any;
  }

  // Status badge color helper
  getStatusBadgeClass(status: string): string {
    const statusClasses: { [key: string]: string } = {
      REPORTED: 'bg-warning',
      PENDING: 'bg-info',
      APPROVED: 'bg-success',
      REJECTED: 'bg-danger',
    };
    return statusClasses[status] || 'bg-secondary';
  }
}