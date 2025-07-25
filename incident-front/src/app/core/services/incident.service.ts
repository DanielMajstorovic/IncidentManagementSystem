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
}
