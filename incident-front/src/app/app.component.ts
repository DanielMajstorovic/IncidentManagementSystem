import { CommonModule } from "@angular/common"
import { Component, OnInit, OnDestroy } from "@angular/core"
import { RouterOutlet, Router, RouterModule } from "@angular/router"
import { Observable, Subject } from "rxjs"
import { takeUntil } from "rxjs/operators"
import { User } from "./core/models/user.model"
import { AuthService } from "./core/services/auth.service"

interface NavigationItem {
  label: string
  route: string
  icon: string
  roles: string[]
}

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet, CommonModule, RouterModule],
  templateUrl: "./app.component.html",
  styleUrl: "./app.component.scss",
})
export class AppComponent implements OnInit, OnDestroy {
  user$: Observable<User | null>
  currentUser: User | null = null
  isMenuOpen = false

  private destroy$ = new Subject<void>()

  // Navigacioni elementi za različite role
  navigationItems: NavigationItem[] = [
    {
      label: "Incidents",
      route: "/moderator/incidents",
      icon: "list-ul",
      roles: ["MODERATOR"],
    },
    {
      label: "Analytics",
      route: "/moderator/analytics",
      icon: "bar-chart",
      roles: ["MODERATOR"],
    },
    {
      label: "Alert Params",
      route: "/moderator/alert-params",
      icon: "bell",
      roles: ["MODERATOR"],
    },
    {
      label: "Admin Incidents",
      route: "/admin/incidents",
      icon: "list-ul",
      roles: ["ADMIN"],
    },
    {
      label: "User Management",
      route: "/admin/user-management",
      icon: "people",
      roles: ["ADMIN"],
    },
  ]

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {
    this.user$ = this.authService.user$
  }

  ngOnInit(): void {
    // Pratimo promene korisnika
    this.user$.pipe(takeUntil(this.destroy$)).subscribe((user) => {
      this.currentUser = user
    })
  }

  ngOnDestroy(): void {
    this.destroy$.next()
    this.destroy$.complete()
  }

  // Dobijamo navigacione elemente na osnovu korisničke role
  getNavigationItems(): NavigationItem[] {
    if (!this.currentUser) {
      return []
    }

    return this.navigationItems.filter((item) => item.roles.includes(this.currentUser!.role))
  }

  // Proveravamo da li je trenutna ruta aktivna
  isRouteActive(route: string): boolean {
    return this.router.url === route
  }

  loginWithGoogle(): void {
    this.authService.loginWithGoogle()
  }

  logout(): void {
    this.authService.logout()
    this.isMenuOpen = false
  }

  toggleMenu(): void {
    this.isMenuOpen = !this.isMenuOpen
  }

  navigateTo(route: string): void {
    this.router.navigate([route])
    this.isMenuOpen = false
  }

  getInitials(name: string): string {
    return name
      .split(" ")
      .map((n) => n[0])
      .join("")
      .toUpperCase()
      .substring(0, 2)
  }

  getRoleColor(role: string): string {
    const roleColors: { [key: string]: string } = {
      ADMIN: "#FFA07A",
      MODERATOR: "#E6E6FA",
      USER: "#F5F5F5",
    }
    return roleColors[role] || "#6c757d"
  }

  getRoleIcon(role: string): string {
    const roleIcons: { [key: string]: string } = {
      ADMIN: "shield-fill-check",
      MODERATOR: "person-gear",
      USER: "eye-fill",
    }
    return roleIcons[role] || "person-circle"
  }

  getRoleDisplayName(role: string): string {
    const roleNames: { [key: string]: string } = {
      ADMIN: "Administrator",
      MODERATOR: "Moderator",
      USER: "User",
    }
    return roleNames[role] || role
  }
}
