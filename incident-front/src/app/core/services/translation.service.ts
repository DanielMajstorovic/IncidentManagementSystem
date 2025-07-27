import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

export interface TranslationRequest {
  text: string;
}

export interface TranslationResponse {
  originalText: string;
  translatedText: string;
  detectedLanguage: string;
  targetLanguage: string;
}

@Injectable({
  providedIn: 'root'
})
export class TranslationService {
  private apiUrl = '/incident-service/api/v1/incidents/translate';

  constructor(private http: HttpClient) { }

  getTranslation(text: string): Observable<TranslationResponse | null> {
    if (!text || text.trim().length === 0) {
      return of(null);
    }
    const requestBody: TranslationRequest = { text };
    return this.http.post<TranslationResponse>(this.apiUrl, requestBody).pipe(
      catchError(error => {
        console.error('Translation via backend failed:', error);
        return of(null);
      })
    );
  }
}