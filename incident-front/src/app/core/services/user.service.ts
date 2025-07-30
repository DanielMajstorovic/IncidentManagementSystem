import { Injectable } from "@angular/core"
import { HttpClient } from "@angular/common/http"
import { Observable, BehaviorSubject } from "rxjs"
import { map, catchError } from "rxjs/operators"

export interface UserDto {
  id: number
  email: string
  name: string
  role: string
  blocked: boolean
  deleted: boolean
}

export interface RoleDto {
  id: number
  name: string
}

// NOVI, OBOGAĆENI DTO za prikaz u tabeli
export interface RoleDetailDto extends RoleDto {
  endpointIds: number[];
  userCount: number; // Broj korisnika sa ovom rolom
}

// NOVI DTO za endpointe
export interface EndpointDto {
  id: number;
  httpMethod: string;
  pathPattern: string;
}

// NOVI DTO za kreiranje role
export interface CreateRoleRequest {
  name: string;
}

// NOVI DTO za ažuriranje dozvola
export interface UpdateRolePermissionsRequest {
  endpointIds: number[];
}

export interface UpdateUserRoleRequest {
  roleName: string
}

@Injectable({
  providedIn: "root",
})
export class UserService {
  private apiUrl = "/user-service/api/v1/users"
  private rolesApiUrl = "/user-service/api/v1/roles"
  private loadingSubject = new BehaviorSubject<boolean>(false)
  public loading$ = this.loadingSubject.asObservable()

  constructor(private http: HttpClient) {}

  /**
   * Gets all users from the system (excluding deleted users)
   * @returns Observable of user list
   */
  getAllUsers(): Observable<UserDto[]> {
    this.loadingSubject.next(true)
    return this.http.get<UserDto[]>(this.apiUrl).pipe(
      map((response) => {
        this.loadingSubject.next(false)
        // Filter out deleted users on the frontend as well
        return response.filter((user) => !user.deleted)
      }),
      catchError((error) => {
        this.loadingSubject.next(false)
        console.error("Failed to get all users:", error)
        throw error
      }),
    )
  }

  /**
   * Gets all available roles
   * @returns Observable of role list
   */
  getAllRoles(): Observable<RoleDto[]> {
    return this.http.get<RoleDto[]>(this.rolesApiUrl).pipe(
      catchError((error) => {
        console.error("Failed to get all roles:", error)
        throw error
      }),
    )
  }

  /**
   * Updates user role
   * @param userId - ID of the user to update
   * @param roleName - New role name
   * @returns Observable of updated user
   */
  updateUserRole(userId: number, roleName: string): Observable<UserDto> {
    const request: UpdateUserRoleRequest = { roleName }
    return this.http.put<UserDto>(`${this.apiUrl}/${userId}/role`, request).pipe(
      catchError((error) => {
        console.error("Failed to update user role:", error)
        throw error
      }),
    )
  }

  /**
   * Blocks a user
   * @param userId - ID of the user to block
   * @returns Observable of updated user
   */
  blockUser(userId: number): Observable<UserDto> {
    return this.http.put<UserDto>(`${this.apiUrl}/${userId}/block`, {}).pipe(
      catchError((error) => {
        console.error("Failed to block user:", error)
        throw error
      }),
    )
  }

  /**
   * Unblocks a user
   * @param userId - ID of the user to unblock
   * @returns Observable of updated user
   */
  unblockUser(userId: number): Observable<UserDto> {
    return this.http.put<UserDto>(`${this.apiUrl}/${userId}/unblock`, {}).pipe(
      catchError((error) => {
        console.error("Failed to unblock user:", error)
        throw error
      }),
    )
  }

  /**
   * Deletes a user (soft delete)
   * @param userId - ID of the user to delete
   * @returns Observable of deletion result
   */
  deleteUser(userId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${userId}`).pipe(
      catchError((error) => {
        console.error("Failed to delete user:", error)
        throw error
      }),
    )
  }

  /**
   * Gets current loading state
   */
  get isLoading(): boolean {
    return this.loadingSubject.value
  }
}
