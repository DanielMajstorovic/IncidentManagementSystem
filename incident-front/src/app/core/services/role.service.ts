import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { CreateRoleRequest, EndpointDto, RoleDto, UpdateRolePermissionsRequest } from './user.service';

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private rolesApiUrl = '/user-service/api/v1/roles';
  private endpointsApiUrl = '/user-service/api/v1/endpoints';

  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {}

  private setLoading(isLoading: boolean): void {
    this.loadingSubject.next(isLoading);
  }

  getAllRoles(): Observable<RoleDto[]> {
    this.setLoading(true);
    return this.http.get<RoleDto[]>(this.rolesApiUrl).pipe(
      tap(() => this.setLoading(false)),
      catchError(error => {
        this.setLoading(false);
        console.error("Failed to get all roles:", error);
        throw error;
      })
    );
  }

  getAllEndpoints(): Observable<EndpointDto[]> {
    return this.http.get<EndpointDto[]>(this.endpointsApiUrl).pipe(
      catchError(error => {
        console.error("Failed to get all endpoints:", error);
        throw error;
      })
    );
  }

  createRole(name: string): Observable<RoleDto> {
    const request: CreateRoleRequest = { name };
    return this.http.post<RoleDto>(this.rolesApiUrl, request).pipe(
      catchError(error => {
        console.error("Failed to create role:", error);
        throw error;
      })
    );
  }

  updateRolePermissions(roleId: number, endpointIds: number[]): Observable<RoleDto> {
    const request: UpdateRolePermissionsRequest = { endpointIds };
    return this.http.put<RoleDto>(`${this.rolesApiUrl}/${roleId}/permissions`, request).pipe(
      catchError(error => {
        console.error("Failed to update role permissions:", error);
        throw error;
      })
    );
  }

  deleteRole(roleId: number): Observable<void> {
    return this.http.delete<void>(`${this.rolesApiUrl}/${roleId}`).pipe(
      catchError(error => {
        console.error("Failed to delete role:", error);
        throw error;
      })
    );
  }
}