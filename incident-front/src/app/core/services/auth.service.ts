import { Injectable } from "@angular/core"
import { BehaviorSubject } from "rxjs"
import { Router } from "@angular/router"
import { User, JwtPayload } from "../models/user.model"

@Injectable({
  providedIn: "root",
})
export class AuthService {
  private readonly TOKEN_KEY = "jwt_token"
  private userSubject = new BehaviorSubject<User | null>(null)
  public user$ = this.userSubject.asObservable()

  constructor(private router: Router) {
    this.initializeAuth()
  }

  private initializeAuth(): void {
    // Proveravamo da li postoji token u localStorage
    const token = this.getToken()
    if (token && this.isTokenValid(token)) {
      const user = this.getUserFromToken(token)
      this.userSubject.next(user)
    } else {
      this.clearToken()
    }

    // Proveravamo da li je token poslat preko URL-a
    this.checkForTokenInUrl()
  }

  private checkForTokenInUrl(): void {
    const urlParams = new URLSearchParams(window.location.search)
    const token = urlParams.get("token")

    if (token) {
      this.setToken(token)
      const user = this.getUserFromToken(token)
      this.userSubject.next(user)

      // Uklanjamo token iz URL-a
      window.history.replaceState({}, document.title, window.location.pathname)

      // Redirektujemo korisnika na osnovu role
      this.redirectUserBasedOnRole(user.role)
    }
  }

  private redirectUserBasedOnRole(role: string): void {
    switch (role) {
      case "ADMIN":
        this.router.navigate(["/admin/incidents"])
        break
      case "MODERATOR":
        this.router.navigate(["/moderator/incidents"])
        break
      case "USER":
        this.router.navigate(["/user-map"])
        break
      default:
        this.router.navigate(["/"])
    }
  }

  public getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY)
  }

  public setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token)
  }

  public clearToken(): void {
    localStorage.removeItem(this.TOKEN_KEY)
    this.userSubject.next(null)
  }

  public isTokenValid(token: string): boolean {
    try {
      const payload = this.decodeToken(token)
      const currentTime = Math.floor(Date.now() / 1000)
      return payload.exp > currentTime
    } catch (error) {
      return false
    }
  }

  private decodeToken(token: string): JwtPayload {
    const base64Url = token.split(".")[1]
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/")
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
        .join(""),
    )
    return JSON.parse(jsonPayload)
  }

  private getUserFromToken(token: string): User {
    const payload = this.decodeToken(token)
    return {
      id: payload.sub,
      name: payload.name,
      email: payload.email,
      role: payload.role,
    }
  }

  public getCurrentUser(): User | null {
    return this.userSubject.value
  }

  public isAuthenticated(): boolean {
    const token = this.getToken()
    return token !== null && this.isTokenValid(token)
  }

  public hasRole(role: string): boolean {
    const user = this.getCurrentUser()
    return user?.role === role
  }

  public hasAnyRole(roles: string[]): boolean {
    const user = this.getCurrentUser()
    return user ? roles.includes(user.role) : false
  }

  public loginWithGoogle(): void {
    window.location.href = "http://localhost:8080/auth-service/oauth2/authorization/google"
  }

  public logout(): void {
    this.clearToken()
    this.router.navigate(["/"])
  }
}
