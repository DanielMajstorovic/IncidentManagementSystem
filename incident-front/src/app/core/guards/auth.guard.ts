import { Injectable } from "@angular/core"
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from "@angular/router"
import { AuthService } from "../services/auth.service"

@Injectable({
  providedIn: "root",
})
export class AuthGuard implements CanActivate {
  constructor(
    private authService: AuthService,
    private router: Router,
  ) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.authService.isAuthenticated()) {
      const requiredRoles = route.data["roles"] as string[]

      if (requiredRoles && requiredRoles.length > 0) {
        if (this.authService.hasAnyRole(requiredRoles)) {
          return true
        } else {
          // Korisnik nema potrebnu rolu
          this.router.navigate(["/unauthorized"])
          return false
        }
      }

      return true
    } else {
      // Korisnik nije autentifikovan
      this.router.navigate(["/"])
      return false
    }
  }
}
