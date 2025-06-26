// Odgovara vašem Page<T> sa backenda
export interface Page<T> {
  content: T[];
  pageable: any;
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: any;
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

// Odgovara vašem Location DTO
export interface Location {
  latitude: number;
  longitude: number;
  radius?: number; // Opciono
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  zipcode?: string;
}

// Odgovara vašem IncidentImage DTO
export interface IncidentImage {
  id?: number; // Opciono pri slanju
  imageUrl: string;
}

// Glavni interfejs za incident koji dobijamo sa servera
export interface Incident {
  id: number;
  type: string; // Enum postaje string
  subtype: string;
  location: Location;
  description: string;
  images: IncidentImage[];
  status: string;
  creationDate: string;
}

// Interfejs za KREIRANJE incidenta, tačno po vašem IncidentRequest.java
export interface IncidentRequest {
  type: string;
  subtype: string;
  location: Location;
  description: string;
  images: IncidentImage[];
}

export interface FilterRequest {
  incidentType?: string;
  incidentSubtype?: string;
  location?: string;
  status?: string;
  timeRange?: string;
}