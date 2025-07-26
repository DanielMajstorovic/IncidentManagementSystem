import {
  Component,
  OnInit,
  OnDestroy,
  ChangeDetectorRef,
  ViewChild,
  ElementRef,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CommonModule, TitleCasePipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { Subject, BehaviorSubject } from 'rxjs';
import { debounceTime, takeUntil, finalize } from 'rxjs/operators';
import { IncidentService } from '../../core/services/incident.service';
import {
  FilterRequest,
  Incident,
  IncidentRequest,
} from '../../core/models/incident.model';
import { ReplacePipe } from '../../core/pipes/replace.pipe';

declare var bootstrap: any;

const iconColorMap: { [key: string]: string } = {
  FIRE: 'red',
  FLOOD: 'blue',
  ACCIDENT: 'orange',
  CRIME: 'black',
  DEFAULT: 'grey',
};

const createColoredIcon = (color: string): L.Icon => {
  return L.icon({
    iconUrl: `https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-${color}.png`,
    shadowUrl:
      'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
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
}

interface SelectedImage {
  file: File;
  preview: string;
  uploaded?: boolean;
  url?: string;
}

interface LocationData {
  latitude: number;
  longitude: number;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  zipcode?: string;
}

@Component({
  selector: 'app-user-map',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    LeafletModule,
    ReplacePipe,
    TitleCasePipe,
    DatePipe,
  ],
  templateUrl: './user-map.component.html',
  styleUrls: ['./user-map.component.scss'],
})
export class UserMapComponent implements OnInit, OnDestroy {
  @ViewChild('fileInput') fileInput!: ElementRef<HTMLInputElement>;

  // Map properties (inherited from main-map)
  map!: L.Map;
  locationMap!: L.Map;
  private locationMarker: L.Marker | null = null;
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

  locationMapOptions: L.MapOptions = {
    layers: [
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap',
      }),
    ],
    zoom: 13, // Povećan zoom za bolji prikaz grada
    center: L.latLng(44.7722, 17.1911), // Koordinate Banja Luke
  };

  incidentLayers: L.Layer[] = [];
  locationLayers: L.Layer[] = [];

  // Filter form (inherited from main-map)
  filterForm: FormGroup;
  incidentTypes = ['FIRE', 'FLOOD', 'ACCIDENT', 'CRIME'];
  incidentSubtypes: { [key: string]: string[] } = {
    FIRE: ['BUILDING_FIRE'],
    ACCIDENT: ['CAR_ACCIDENT'],
    CRIME: ['ROBBERY', 'ASSAULT'],
  };
  currentSubtypes: string[] = [];
  currentReportSubtypes: string[] = [];

  timeRanges = [
    { value: '1h', label: 'Last Hour', icon: 'clock' },
    { value: '24h', label: 'Last 24H', icon: 'clock-history' },
    { value: '7d', label: 'Last 7D', icon: 'calendar-week' },
    { value: '31d', label: 'Last Month', icon: 'calendar-month' },
  ];

  legendData = Object.keys(iconColorMap)
    .filter((key) => key !== 'DEFAULT')
    .map((key) => ({
      type: key,
      color: iconColorMap[key],
      icon: this.getTypeIcon(key),
    }));

  // Modal state management (inherited from main-map)
  private modalStateSubject = new BehaviorSubject<ModalState>({
    isOpen: false,
    isLoading: false,
    incident: null,
    imageUrls: [],
    currentImageIndex: 0,
    error: null,
  });
  public modalState$ = this.modalStateSubject.asObservable();
  public currentModalState: ModalState = this.modalStateSubject.value;

  // Report incident properties
  reportForm: FormGroup;
  currentStep = 1;
  selectedLocation: LocationData | null = null;
  selectedImages: SelectedImage[] = [];
  locationSearchQuery = '';
  isDragOver = false;
  uploadProgress = 0;
  isSubmitting = false;

  // Loading states
  public isMapLoading = true;
  public isFilterCollapsed = false;
  public activeFiltersCount = 0;

  // Modals
  private incidentModal: any;
  private reportModal: any;
  private destroy$ = new Subject<void>();

  constructor(
    private incidentService: IncidentService,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {
    // Initialize filter form (inherited from main-map)
    this.filterForm = this.fb.group({
      incidentType: [''],
      incidentSubtype: [''],
      location: [''],
      timeRange: [''],
    });

    // Initialize report form
    this.reportForm = this.fb.group({
      type: ['', Validators.required],
      subtype: [''],
      latitude: [null, Validators.required],
      longitude: [null, Validators.required],
      address: [''],
      city: [''],
      state: [''],
      country: [''],
      zipcode: [''],
      description: ['', [Validators.required, Validators.minLength(10)]],
    });

    // Subscribe to modal state changes
    this.modalState$.pipe(takeUntil(this.destroy$)).subscribe((state) => {
      this.currentModalState = state;
      this.cdr.detectChanges();
    });
  }

  ngOnInit(): void {
    this.initializeModals();
    this.loadIncidents();
    this.setupFormSubscription();
    this.updateActiveFiltersCount();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  // Inherited methods from main-map component
  private setupFormSubscription(): void {
    this.filterForm.valueChanges
      .pipe(debounceTime(200), takeUntil(this.destroy$))
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

  private setDefaultLocationPin(): void {
    const banjaLukaCoords = { lat: 44.7722, lng: 17.1911 };
    this.setSelectedLocation(banjaLukaCoords.lat, banjaLukaCoords.lng);
  }

  onLocationMapReady(map: L.Map): void {
    this.locationMap = map;

    // Postavite default pin u centru Banja Luke
    this.setDefaultLocationPin();
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

  onReportTypeChange(): void {
    const selectedType = this.reportForm.get('type')?.value;
    this.currentReportSubtypes =
      this.incidentSubtypes[selectedType || ''] || [];
    this.reportForm.get('subtype')?.setValue('');
  }

  resetFilters(): void {
    this.filterForm.reset({
      incidentType: '',
      incidentSubtype: '',
      location: '',
      timeRange: '',
    });
  }

  toggleFilterPanel(): void {
    this.isFilterCollapsed = !this.isFilterCollapsed;
  }

  // Map click handlers
  onMapClick(event: any): void {
    const leafletEvent = event as L.LeafletMouseEvent;
    // Handle main map clicks if needed
  }

  onLocationMapClick(event: any): void {
    const leafletEvent = event as L.LeafletMouseEvent;
    const { lat, lng } = leafletEvent.latlng;
    this.setSelectedLocation(lat, lng);
  }

  private async setSelectedLocation(lat: number, lng: number): Promise<void> {
    this.selectedLocation = { latitude: lat, longitude: lng };

    // Update form with coordinates
    this.reportForm.patchValue({
      latitude: lat,
      longitude: lng,
    });

    // Remove existing marker if it exists
    if (this.locationMarker) {
      this.locationMap.removeLayer(this.locationMarker);
    }

    // Clear existing location layers
    this.locationLayers = [];

    // Create draggable marker
    this.locationMarker = L.marker([lat, lng], {
      icon: createColoredIcon('green'),
      draggable: true, // Omogući dragging
    });

    // Add drag event listener
    this.locationMarker.on('dragend', (event: any) => {
      const marker = event.target;
      const position = marker.getLatLng();
      this.onMarkerDragEnd(position.lat, position.lng);
    });

    // Add marker to map
    this.locationMarker.addTo(this.locationMap);
    this.locationLayers.push(this.locationMarker);

    // Try to get address information via reverse geocoding
    try {
      const addressData = await this.reverseGeocode(lat, lng);
      if (addressData) {
        this.reportForm.patchValue({
          address: addressData.address || '',
          city: addressData.city || '',
          state: addressData.state || '',
          country: addressData.country || '',
          zipcode: addressData.zipcode || '',
        });

        this.selectedLocation = { ...this.selectedLocation, ...addressData };
      }
    } catch (error) {
      console.warn('Reverse geocoding failed:', error);
    }
  }

  private async onMarkerDragEnd(lat: number, lng: number): Promise<void> {
    this.selectedLocation = { latitude: lat, longitude: lng };

    // Update form with new coordinates
    this.reportForm.patchValue({
      latitude: lat,
      longitude: lng,
    });

    // Trigger change detection
    this.cdr.detectChanges();

    // Try to get address information for new position
    try {
      const addressData = await this.reverseGeocode(lat, lng);
      if (addressData) {
        this.reportForm.patchValue({
          address: addressData.address || '',
          city: addressData.city || '',
          state: addressData.state || '',
          country: addressData.country || '',
          zipcode: addressData.zipcode || '',
        });

        this.selectedLocation = { ...this.selectedLocation, ...addressData };
      }
    } catch (error) {
      console.warn('Reverse geocoding failed:', error);
    }
  }

  private async reverseGeocode(lat: number, lng: number): Promise<any> {
    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&addressdetails=1`
      );
      const data = await response.json();

      if (data && data.address) {
        return {
          address: data.display_name?.split(',')[0] || '',
          city:
            data.address.city ||
            data.address.town ||
            data.address.village ||
            '',
          state: data.address.state || data.address.region || '',
          country: data.address.country || '',
          zipcode: data.address.postcode || '',
        };
      }
    } catch (error) {
      console.error('Reverse geocoding error:', error);
    }
    return null;
  }

  async searchLocation(): Promise<void> {
    if (!this.locationSearchQuery.trim()) return;

    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(
          this.locationSearchQuery
        )}&limit=1&addressdetails=1`
      );
      const data = await response.json();

      if (data && data.length > 0) {
        const result = data[0];
        const lat = Number.parseFloat(result.lat);
        const lng = Number.parseFloat(result.lon);

        // Center location map on found location
        if (this.locationMap) {
          this.locationMap.setView([lat, lng], 15);
        }

        // Update the existing marker position
        await this.setSelectedLocation(lat, lng);
      }
    } catch (error) {
      console.error('Location search error:', error);
    }
  }
  getCurrentLocation(): void {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const lat = position.coords.latitude;
          const lng = position.coords.longitude;

          // Center location map on current location
          if (this.locationMap) {
            this.locationMap.setView([lat, lng], 15);
          }

          // Update the existing marker position
          await this.setSelectedLocation(lat, lng);
        },
        (error) => {
          console.error('Geolocation error:', error);
          alert(
            'Unable to get your current location. Please select a location on the map or search for an address.'
          );
        }
      );
    } else {
      alert('Geolocation is not supported by this browser.');
    }
  }

  // Modal management
  private initializeModals(): void {
    // Initialize incident detail modal
    const incidentModalElement = document.getElementById('incidentDetailModal');
    if (incidentModalElement) {
      this.incidentModal = new bootstrap.Modal(incidentModalElement);
      incidentModalElement.addEventListener('hidden.bs.modal', () => {
        this.updateModalState({
          isOpen: false,
          isLoading: false,
          incident: null,
          imageUrls: [],
          currentImageIndex: 0,
          error: null,
        });
      });
    }

    // Initialize report modal
    const reportModalElement = document.getElementById('reportIncidentModal');
    if (reportModalElement) {
      this.reportModal = new bootstrap.Modal(reportModalElement);
      reportModalElement.addEventListener('hidden.bs.modal', () => {
        if (this.currentStep !== 5) {
          this.resetReportForm();
        }
      });
    }
  }

  openReportModal(): void {
    this.resetReportForm();
    this.reportModal?.show();
  }

  // Stepper navigation
  canProceedToNextStep(): boolean {
    switch (this.currentStep) {
      case 1:
        return this.reportForm.get('type')?.valid || false;
      case 2:
        return (
          (this.selectedLocation !== null &&
            this.reportForm.get('latitude')?.valid &&
            this.reportForm.get('longitude')?.valid) ||
          false
        );
      case 3:
        return this.reportForm.get('description')?.valid || false;
      case 4:
        return true; // Images are optional
      default:
        return false;
    }
  }

  canSubmitReport(): boolean {
    return this.reportForm.valid && this.selectedLocation !== null;
  }

  nextStep(): void {
    if (this.canProceedToNextStep() && this.currentStep < 4) {
      this.currentStep++;
    }
  }

  previousStep(): void {
    if (this.currentStep > 1) {
      this.currentStep--;
    }
  }

  // Image handling
  onFileSelect(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files) {
      this.handleFiles(Array.from(input.files));
    }
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = true;
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragOver = false;

    if (event.dataTransfer?.files) {
      this.handleFiles(Array.from(event.dataTransfer.files));
    }
  }

  private handleFiles(files: File[]): void {
    const validFiles = files.filter((file) => {
      // Check file type
      if (!file.type.startsWith('image/')) {
        alert(`${file.name} is not a valid image file.`);
        return false;
      }

      // Check file size (5MB limit)
      if (file.size > 5 * 1024 * 1024) {
        alert(`${file.name} is too large. Maximum file size is 5MB.`);
        return false;
      }

      return true;
    });

    validFiles.forEach((file) => {
      const reader = new FileReader();
      reader.onload = (e) => {
        this.selectedImages.push({
          file: file,
          preview: e.target?.result as string,
          uploaded: false,
        });
        this.cdr.detectChanges();
      };
      reader.readAsDataURL(file);
    });
  }

  removeImage(index: number): void {
    this.selectedImages.splice(index, 1);
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return (
      Number.parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    );
  }

  // Submit report
  async submitReport(): Promise<void> {
    if (!this.canSubmitReport() || this.isSubmitting) return;

    this.isSubmitting = true;
    this.uploadProgress = 0;

    try {
      // Upload images first if any
      let uploadedImageUrls: string[] = [];

      if (this.selectedImages.length > 0) {
        uploadedImageUrls = await this.uploadImages();
      }

      // Prepare incident request
      const incidentRequest: IncidentRequest = {
        type: this.reportForm.get('type')?.value,
        subtype: this.reportForm.get('subtype')?.value || null,
        location: {
          latitude: this.reportForm.get('latitude')?.value,
          longitude: this.reportForm.get('longitude')?.value,
          radius: 0,
          address: this.reportForm.get('address')?.value || '',
          city: this.reportForm.get('city')?.value || '',
          state: this.reportForm.get('state')?.value || '',
          country: this.reportForm.get('country')?.value || '',
          zipcode: this.reportForm.get('zipcode')?.value || '',
        },
        description: this.reportForm.get('description')?.value,
        images: uploadedImageUrls.map((url) => ({ imageUrl: url })),
      };

      // Submit incident
      await this.incidentService.createIncident(incidentRequest).toPromise();

      // Move to success step
      this.currentStep = 5;

      // Reload incidents to show the new one (if approved)
      this.loadIncidents();
    } catch (error) {
      console.error('Failed to submit incident:', error);
      alert('Failed to submit incident. Please try again.');
    } finally {
      this.isSubmitting = false;
      this.uploadProgress = 0;
    }
  }

  private async uploadImages(): Promise<string[]> {
    const formData = new FormData();

    this.selectedImages.forEach((image, index) => {
      formData.append('file', image.file);
    });

    try {
      this.uploadProgress = 50; // Simulate progress
      const response = await this.incidentService
        .uploadFiles(formData)
        .toPromise();
      this.uploadProgress = 100;
      return response || [];
    } catch (error) {
      console.error('Failed to upload images:', error);
      throw error;
    }
  }

  resetReportForm(): void {
    this.currentStep = 1;
    this.selectedLocation = null;
    this.selectedImages = [];
    this.locationSearchQuery = '';
    this.isDragOver = false;
    this.uploadProgress = 0;
    this.isSubmitting = false;
    this.locationLayers = [];

    // Remove existing marker
    if (this.locationMarker && this.locationMap) {
      this.locationMap.removeLayer(this.locationMarker);
      this.locationMarker = null;
    }

    this.reportForm.reset({
      type: '',
      subtype: '',
      latitude: null,
      longitude: null,
      address: '',
      city: '',
      state: '',
      country: '',
      zipcode: '',
      description: '',
    });

    this.currentReportSubtypes = [];
  }

  // Inherited methods from main-map component
  openIncidentModal(incident: Incident): void {
    this.updateModalState({
      isOpen: true,
      isLoading: true,
      incident: incident,
      imageUrls: [],
      currentImageIndex: 0,
      error: null,
    });
    this.incidentModal?.show();

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

  private loadIncidents(): void {
    const rawFilters = this.filterForm.value;
    const cleanFilters: Partial<FilterRequest> = {};
    Object.keys(rawFilters).forEach((key) => {
      const value = rawFilters[key];
      if (value !== '' && value !== null) {
        (cleanFilters as any)[key] = value;
      }
    });

    const request: FilterRequest = { ...cleanFilters, status: 'APPROVED' };

    this.incidentService.filterIncidents(request).subscribe({
      next: (response) => {
        this.incidentLayers = [];
        const markerPoints: L.LatLng[] = [];

        response.content.forEach((incident) => {
          const point = L.latLng(
            incident.location.latitude,
            incident.location.longitude
          );
          markerPoints.push(point);

          const color = iconColorMap[incident.type] || iconColorMap['DEFAULT'];
          const icon = createColoredIcon(color);
          const marker = L.marker(point, { icon });

          marker.on('mouseover', () => {
            marker
              .bindTooltip(`${incident.type}: ${incident.description}`, {
                permanent: false,
                direction: 'bottom',
              })
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
      },
    });
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
      console.log('Coordinates copied to clipboard');
    } catch (err) {
      console.error('Failed to copy coordinates:', err);
      const textArea = document.createElement('textarea');
      textArea.value = coordinates;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
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

  get isLoading(): boolean {
    return this.incidentService.loading$ as any;
  }
}
