import { CommonModule } from "@angular/common"
import { Component } from "@angular/core"
import { RouterOutlet } from "@angular/router"
import { Observable } from "rxjs"

@Component({
  selector: "app-root",
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: "./app.component.html",
  styleUrl: "./app.component.scss",
})
export class AppComponent {
  user$: Observable<any>
  isMenuOpen = false

  constructor() {
    this.user$ = new Observable((observer) => {
      // Simulacija korisničkih podataka, zameni sa stvarnim servisom
      // const user = {
      //   name: "John Doe",
      //   email: "john.doe@example.com",
      //   role: "Manager",
      //   avatar: null, // URL slike ili null za inicijale
      // }

      const user = null; // Uncomment za testiranje login state-a

      observer.next(user)
      observer.complete()
    })
  }

  loginWithGoogle() {
    window.location.href = "/api/auth/login" // ili gateway ruta
  }

  logout() {
    // Implementiraj logout logiku
    console.log("Logging out...")
  }

  toggleMenu() {
    this.isMenuOpen = !this.isMenuOpen
  }

  getInitials(name: string): string {
    return name
      .split(" ")
      .map((n) => n[0])
      .join("")
      .toUpperCase()
      .substring(0, 2) // Maksimalno 2 slova
  }

  getRoleColor(role: string): string {
    const roleColors: { [key: string]: string } = {
      Administrator: "#e74c3c",
      Moderator: "#f39c12",
      User: "#95a5a6",
    }
    return roleColors[role] || "#6c757d"
  }

  getRoleIcon(role: string): string {
    const roleIcons: { [key: string]: string } = {
      Administrator: "shield-fill-check",
      Moderator: "person-gear",
      User: "eye-fill",
    }
    return roleIcons[role] || "person-circle"
  }
}
