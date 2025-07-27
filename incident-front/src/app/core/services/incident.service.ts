import { Injectable } from "@angular/core"
import { HttpClient, HttpParams } from "@angular/common/http"
import { Observable, BehaviorSubject, of } from "rxjs"
import { map, catchError, shareReplay } from "rxjs/operators"
import { Incident, Page, FilterRequest, IncidentRequest } from "../models/incident.model"

@Injectable({
  providedIn: "root",
})
export class IncidentService {
  private apiUrl = "/incident-service/api/v1/incidents"
  private loadingSubject = new BehaviorSubject<boolean>(false)
  private imageUrlCache = new Map<string, Observable<string>>()
  public loading$ = this.loadingSubject.asObservable()

  constructor(private http: HttpClient) {}

  filterIncidents(filter: FilterRequest): Observable<Page<Incident>> {
    this.loadingSubject.next(true)
    const pageParams = new HttpParams().set("page", "0").set("size", "1000")
    return this.http.post<Page<Incident>>(`${this.apiUrl}/filter`, filter, { params: pageParams }).pipe(
      map((response) => {
        this.loadingSubject.next(false)
        return response
      }),
      catchError((error) => {
        this.loadingSubject.next(false)
        throw error
      }),
    )
  }

  createIncident(request: IncidentRequest): Observable<Incident> {
    return this.http.post<Incident>(this.apiUrl, request)
  }

  uploadFiles(formData: FormData): Observable<string[]> {
    return this.http.post<string[]>(`${this.apiUrl}/upload`, formData)
  }

  // Optimizovano sa caching-om
  getPresignedImageUrl(filename: string): Observable<string> {
    const fileName = filename.split("/").pop() || filename
    // Proverava cache
    if (this.imageUrlCache.has(fileName)) {
      return this.imageUrlCache.get(fileName)!
    }
    // Kreira novi request sa caching-om
    const request$ = this.http.get(`${this.apiUrl}/image-url/${fileName}`, { responseType: "text" }).pipe(
      catchError(() => of("/assets/image-error.png")),
      shareReplay(1), // Cache rezultat
    )
    this.imageUrlCache.set(fileName, request$)
    return request$
  }

  // Metoda za preload-ovanje slika
  preloadImages(images: string[]): Observable<string[]> {
    const requests = images.map((img) => this.getPresignedImageUrl(img))
    return new Observable((observer) => {
      const results: string[] = []
      let completed = 0
      requests.forEach((request, index) => {
        request.subscribe({
          next: (url) => {
            results[index] = url
            completed++
            if (completed === requests.length) {
              observer.next(results)
              observer.complete()
            }
          },
          error: () => {
            results[index] = "/assets/image-error.png"
            completed++
            if (completed === requests.length) {
              observer.next(results)
              observer.complete()
            }
          },
        })
      })
    })
  }

  // ============ MODERATION METHODS ============

  /**
   * Updates the status of an incident (for moderation purposes)
   * @param incidentId - ID of the incident to update
   * @param status - New status to set (APPROVED, REJECTED, etc.)
   * @returns Observable of the updated incident
   */
  updateIncidentStatus(incidentId: number, status: string): Observable<Incident> {
    const updateRequest = { status: status }
    return this.http.put<Incident>(`${this.apiUrl}/${incidentId}/status`, updateRequest).pipe(
      catchError((error) => {
        console.error('Failed to update incident status:', error)
        throw error
      })
    )
  }

  /**
   * Gets a single incident by ID (useful for detailed moderation view)
   * @param incidentId - ID of the incident to retrieve
   * @returns Observable of the incident
   */
  getIncidentById(incidentId: number): Observable<Incident> {
    return this.http.get<Incident>(`${this.apiUrl}/${incidentId}`).pipe(
      catchError((error) => {
        console.error('Failed to get incident by ID:', error)
        throw error
      })
    )
  }

  /**
   * Gets incidents by status (useful for moderation filters)
   * @param status - Status to filter by (REPORTED, PENDING, APPROVED, REJECTED, etc.)
   * @param page - Page number (default: 0)
   * @param size - Page size (default: 1000)
   * @returns Observable of paginated incidents
   */
  getIncidentsByStatus(status: string, page: number = 0, size: number = 1000): Observable<Page<Incident>> {
    this.loadingSubject.next(true)
    const params = new HttpParams()
      .set("status", status)
      .set("page", page.toString())
      .set("size", size.toString())
    
    return this.http.get<Page<Incident>>(this.apiUrl, { params }).pipe(
      map((response) => {
        this.loadingSubject.next(false)
        return response
      }),
      catchError((error) => {
        this.loadingSubject.next(false)
        console.error('Failed to get incidents by status:', error)
        throw error
      })
    )
  }

  /**
   * Bulk approval of multiple incidents
   * @param incidentIds - Array of incident IDs to approve
   * @returns Observable of update results
   */
  bulkApproveIncidents(incidentIds: number[]): Observable<any> {
    const requests = incidentIds.map(id => this.updateIncidentStatus(id, 'APPROVED'))
    return new Observable((observer) => {
      const results: any[] = []
      let completed = 0
      
      requests.forEach((request, index) => {
        request.subscribe({
          next: (result) => {
            results[index] = { success: true, incident: result }
            completed++
            if (completed === requests.length) {
              observer.next(results)
              observer.complete()
            }
          },
          error: (error) => {
            results[index] = { success: false, error: error }
            completed++
            if (completed === requests.length) {
              observer.next(results)
              observer.complete()
            }
          }
        })
      })
    })
  }

  /**
   * Bulk rejection of multiple incidents
   * @param incidentIds - Array of incident IDs to reject
   * @returns Observable of update results
   */
  bulkRejectIncidents(incidentIds: number[]): Observable<any> {
    const requests = incidentIds.map(id => this.updateIncidentStatus(id, 'REJECTED'))
    return new Observable((observer) => {
      const results: any[] = []
      let completed = 0
      
      requests.forEach((request, index) => {
        request.subscribe({
          next: (result) => {
            results[index] = { success: true, incident: result }
            completed++
            if (completed === requests.length) {
              observer.next(results)
              observer.complete()
            }
          },
          error: (error) => {
            results[index] = { success: false, error: error }
            completed++
            if (completed === requests.length) {
              observer.next(results)
              observer.complete()
            }
          }
        })
      })
    })
  }

  /**
   * Gets moderation statistics
   * @returns Observable with counts of incidents by status
   */
  getModerationStats(): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/moderation/stats`).pipe(
      catchError((error) => {
        console.error('Failed to get moderation stats:', error)
        // Return default stats if endpoint doesn't exist
        return of({
          total: 0,
          reported: 0,
          pending: 0,
          approved: 0,
          rejected: 0
        })
      })
    )
  }

  /**
   * Searches incidents for moderation with advanced filters
   * @param searchTerm - Search term to look for in description or location
   * @param filters - Additional filters (type, status, date range, etc.)
   * @returns Observable of filtered incidents
   */
  searchIncidentsForModeration(searchTerm: string, filters: Partial<FilterRequest> = {}): Observable<Page<Incident>> {
    this.loadingSubject.next(true)
    
    const searchFilters: FilterRequest = {
      ...filters,
      // Add search functionality if backend supports it
      // searchTerm: searchTerm
    }

    const pageParams = new HttpParams().set("page", "0").set("size", "1000")
    
    return this.http.post<Page<Incident>>(`${this.apiUrl}/filter`, searchFilters, { params: pageParams }).pipe(
      map((response) => {
        // Client-side filtering by search term if backend doesn't support it
        if (searchTerm && searchTerm.trim() !== '') {
          const filteredContent = response.content.filter(incident => 
            incident.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
            incident.location.city?.toLowerCase().includes(searchTerm.toLowerCase()) ||
            incident.location.address?.toLowerCase().includes(searchTerm.toLowerCase())
          )
          response.content = filteredContent
          response.totalElements = filteredContent.length
        }
        
        this.loadingSubject.next(false)
        return response
      }),
      catchError((error) => {
        this.loadingSubject.next(false)
        console.error('Failed to search incidents for moderation:', error)
        throw error
      })
    )
  }

  /**
   * Clears the image URL cache (useful when images are updated)
   */
  clearImageCache(): void {
    this.imageUrlCache.clear()
  }

  /**
   * Gets current loading state
   */
  get isLoading(): boolean {
    return this.loadingSubject.value
  }
}