import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { map, catchError } from 'rxjs/operators';

export interface UserDto {
  id: number;
  email: string;
  name: string;
  role: string;
  blocked: boolean;
  deleted: boolean;
}

export interface RoleDto {
  id: number;
  name: string;
}

export interface UpdateUserRoleRequest {
  roleName: string;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private apiUrl = '/user-service/api/v1/users';
  private rolesApiUrl = '/user-service/api/v1/roles';
  private loadingSubject = new BehaviorSubject<boolean>(false);
  public loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Gets all users from the system
   * @returns Observable of user list
   */
  getAllUsers(): Observable<UserDto[]> {
    this.loadingSubject.next(true);
    return this.http.get<UserDto[]>(this.apiUrl).pipe(
      map((response) => {
        this.loadingSubject.next(false);
        return response;
      }),
      catchError((error) => {
        this.loadingSubject.next(false);
        console.error('Failed to get all users:', error);
        throw error;
      })
    );
  }

  /**
   * Gets all available roles
   * @returns Observable of role list
   */
  getAllRoles(): Observable<RoleDto[]> {
    return this.http.get<RoleDto[]>(this.rolesApiUrl).pipe(
      catchError((error) => {
        console.error('Failed to get all roles:', error);
        throw error;
      })
    );
  }

  /**
   * Updates user role
   * @param userId - ID of the user to update
   * @param roleName - New role name
   * @returns Observable of updated user
   */
  updateUserRole(userId: number, roleName: string): Observable<UserDto> {
    const request: UpdateUserRoleRequest = { roleName };
    return this.http.put<UserDto>(`${this.apiUrl}/${userId}/role`, request).pipe(
      catchError((error) => {
        console.error('Failed to update user role:', error);
        throw error;
      })
    );
  }

  /**
   * Blocks a user
   * @param userId - ID of the user to block
   * @returns Observable of updated user
   */
  blockUser(userId: number): Observable<UserDto> {
    return this.http.put<UserDto>(`${this.apiUrl}/${userId}/block`, {}).pipe(
      catchError((error) => {
        console.error('Failed to block user:', error);
        throw error;
      })
    );
  }

  /**
   * Unblocks a user
   * @param userId - ID of the user to unblock
   * @returns Observable of updated user
   */
  unblockUser(userId: number): Observable<UserDto> {
    return this.http.put<UserDto>(`${this.apiUrl}/${userId}/unblock`, {}).pipe(
      catchError((error) => {
        console.error('Failed to unblock user:', error);
        throw error;
      })
    );
  }

  /**
   * Deletes a user (soft delete)
   * @param userId - ID of the user to delete
   * @returns Observable of deletion result
   */
  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}`).pipe(
      catchError((error) => {
        console.error('Failed to delete user:', error);
        throw error;
      })
    );
  }

  /**
   * Bulk operations for multiple users
   */
  bulkBlockUsers(userIds: number[]): Observable<any> {
    const requests = userIds.map(id => this.blockUser(id));
    return new Observable((observer) => {
      const results: any[] = [];
      let completed = 0;
      
      requests.forEach((request, index) => {
        request.subscribe({
          next: (result) => {
            results[index] = { success: true, user: result };
            completed++;
            if (completed === requests.length) {
              observer.next(results);
              observer.complete();
            }
          },
          error: (error) => {
            results[index] = { success: false, error: error };
            completed++;
            if (completed === requests.length) {
              observer.next(results);
              observer.complete();
            }
          }
        });
      });
    });
  }

  bulkUnblockUsers(userIds: number[]): Observable<any> {
    const requests = userIds.map(id => this.unblockUser(id));
    return new Observable((observer) => {
      const results: any[] = [];
      let completed = 0;
      
      requests.forEach((request, index) => {
        request.subscribe({
          next: (result) => {
            results[index] = { success: true, user: result };
            completed++;
            if (completed === requests.length) {
              observer.next(results);
              observer.complete();
            }
          },
          error: (error) => {
            results[index] = { success: false, error: error };
            completed++;
            if (completed === requests.length) {
              observer.next(results);
              observer.complete();
            }
          }
        });
      });
    });
  }

  /**
   * Gets current loading state
   */
  get isLoading(): boolean {
    return this.loadingSubject.value;
  }
}