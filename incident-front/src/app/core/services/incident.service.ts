import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Incident, Page, FilterRequest } from '../models/incident.model';

@Injectable({
  providedIn: 'root'
})
export class IncidentService {

  private apiUrl = '/incident-service/api/v1/incidents';

  constructor(private http: HttpClient) { }

  // Ova metoda ostaje ista
  filterIncidents(filter: FilterRequest): Observable<Page<Incident>> {
    const pageParams = new HttpParams()
      .set('page', '0')
      .set('size', '1000');

    return this.http.post<Page<Incident>>(`${this.apiUrl}/filter`, filter, { params: pageParams });
  }

  // NOVA METODA - za dobijanje privremenog URL-a za jednu sliku
  getPresignedImageUrl(filename: string): Observable<string> {

    const fileName = filename.split('/').pop();
    // Važno je da backend vraća URL kao čist string (text/plain)
    return this.http.get(`${this.apiUrl}/image-url/${fileName}`, { responseType: 'text' });
  }
}