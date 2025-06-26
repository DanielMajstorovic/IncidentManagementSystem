import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, TitleCasePipe, DatePipe } from '@angular/common';
import * as L from 'leaflet';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';
import { Subject, forkJoin, of } from 'rxjs';
import { debounceTime, takeUntil, catchError } from 'rxjs/operators';

import { IncidentService } from '../../core/services/incident.service';
import { FilterRequest, Incident } from '../../core/models/incident.model';
import { ReplacePipe } from '../../core/pipes/replace.pipe';

declare var bootstrap: any;

const iconColorMap: { [key: string]: string } = {
  FIRE: 'red', FLOOD: 'blue', ACCIDENT: 'orange', CRIME: 'black', DEFAULT: 'grey'
};

const createColoredIcon = (color: string): L.Icon => {
  return L.icon({
    iconUrl: `https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-${color}.png`,
    shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
    iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34], shadowSize: [41, 41]
  });
};

@Component({
  selector: 'app-main-map',
  standalone: true,
  imports: [ CommonModule, ReactiveFormsModule, LeafletModule, ReplacePipe, TitleCasePipe, DatePipe ],
  templateUrl: './main-map.component.html',
  styleUrls: ['./main-map.component.scss']
})
export class MainMapComponent implements OnInit, OnDestroy {
  map!: L.Map;
  mapOptions: L.MapOptions = {
    layers: [L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '© OpenStreetMap' })],
    zoom: 7, center: L.latLng(44.2, 17.8)
  };
  incidentLayers: L.Layer[] = [];
  filterForm: FormGroup;
  incidentTypes = ['FIRE', 'FLOOD', 'ACCIDENT', 'CRIME'];
  incidentSubtypes: { [key: string]: string[] } = {
    FIRE: ['BUILDING_FIRE'], ACCIDENT: ['CAR_ACCIDENT'], CRIME: ['ROBBERY', 'ASSAULT']
  };
  currentSubtypes: string[] = [];
  timeRanges = [
    { value: '1h', label: 'Last Hour' }, { value: '24h', label: 'Last 24H' },
    { value: '7d', label: 'Last 7D' }, { value: '31d', label: 'Last Month' }
  ];
  legendData = Object.keys(iconColorMap).map(key => ({ type: key, color: iconColorMap[key] }));

  public selectedIncident: Incident | null = null;
  public presignedImageUrls: string[] = [];
  public isModalLoading = false; // Stanje za prikaz spinnera u modalu
  private incidentModal: any;
  private destroy$ = new Subject<void>();

  constructor(private incidentService: IncidentService, private fb: FormBuilder) {
    this.filterForm = this.fb.group({
      incidentType: [''], incidentSubtype: [''], location: [''], timeRange: ['']
    });
  }

  ngOnInit(): void {
    this.initializeModal();
    this.loadIncidents();
    this.filterForm.valueChanges.pipe(
      debounceTime(500),
      takeUntil(this.destroy$)
    ).subscribe(() => this.loadIncidents());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onMapReady(map: L.Map): void { this.map = map; }

  onTypeChange(): void {
    const selectedType = this.filterForm.get('incidentType')?.value;
    this.currentSubtypes = this.incidentSubtypes[selectedType || ''] || [];
    this.filterForm.get('incidentSubtype')?.setValue('');
  }

  resetFilters(): void {
    this.filterForm.reset({ incidentType: '', incidentSubtype: '', location: '', timeRange: '' });
  }

  // JEDINA IZMENA JE U OVE DVE METODE ISPOD
  openIncidentModal(incident: Incident): void {
    this.selectedIncident = incident;
    this.presignedImageUrls = []; // Resetuj prethodne URL-ove
    this.isModalLoading = true; // Pokaži spinner
    this.incidentModal?.show(); // Odmah prikaži modal sa spinnerom

    if (incident.images && incident.images.length > 0) {
      // Kreiramo niz Observable-a, po jedan za svaku sliku
      const urlObservables = incident.images.map(image =>
        this.incidentService.getPresignedImageUrl(image.imageUrl).pipe(
          catchError(err => {
            console.error(`Failed to get presigned URL for ${image.imageUrl}`, err);
            return of('assets/image-error.png'); // Vrati fallback sliku u slučaju greške
          })
        )
      );

      // forkJoin čeka da se svi API pozivi završe
      forkJoin(urlObservables).subscribe(urls => {
        this.presignedImageUrls = urls;
        this.isModalLoading = false; // Sakrij spinner
      });
    } else {
      this.isModalLoading = false; // Nema slika, sakrij spinner
    }
  }

  private loadIncidents(): void {
    const rawFilters = this.filterForm.value;
    const cleanFilters: Partial<FilterRequest> = {};
    Object.keys(rawFilters).forEach(key => {
      const value = rawFilters[key];
      if (value !== '' && value !== null) {
        (cleanFilters as any)[key] = value;
      }
    });

    const request: FilterRequest = { ...cleanFilters, status: 'APPROVED' };
    
    this.incidentService.filterIncidents(request).subscribe(response => {
      this.incidentLayers = [];
      const markerPoints: L.LatLng[] = [];
      response.content.forEach(incident => {
        const point = L.latLng(incident.location.latitude, incident.location.longitude);
        markerPoints.push(point);
        const color = iconColorMap[incident.type] || iconColorMap['DEFAULT'];
        const icon = createColoredIcon(color);
        const marker = L.marker(point, { icon });
        marker.on('click', () => this.openIncidentModal(incident));
        this.incidentLayers.push(marker);
      });

      if (this.map && markerPoints.length > 0) {
        const bounds = L.latLngBounds(markerPoints);
        this.map.fitBounds(bounds, { padding: [50, 50] });
      }
    });
  }

  private initializeModal(): void {
    const modalElement = document.getElementById('incidentDetailModal');
    if (modalElement) {
      this.incidentModal = new bootstrap.Modal(modalElement);
    }
  }
}