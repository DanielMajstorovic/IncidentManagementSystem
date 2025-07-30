import { Component, type OnInit, type OnDestroy } from "@angular/core"
import { CommonModule } from "@angular/common"
import { FormsModule } from "@angular/forms"
import { Subject } from "rxjs"
import { takeUntil } from "rxjs/operators"
import { RoleDto, UserDto, UserService } from "../../core/services/user.service"

@Component({
  selector: "app-user-management",
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./user-management.component.html",
  styleUrls: ["./user-management.component.scss"],
})
export class UserManagementComponent implements OnInit, OnDestroy {
  users: UserDto[] = []
  roles: RoleDto[] = []
  filteredUsers: UserDto[] = []

  // Search and filter
  searchTerm = ""
  selectedRoleFilter = ""
  selectedStatusFilter = ""

  // Modal states
  showDeleteModal = false
  showRoleModal = false
  userToDelete: UserDto | null = null
  userToUpdateRole: UserDto | null = null
  newRoleForUser = ""

  // Loading and error states
  loading = false
  error = ""
  successMessage = ""

  private destroy$ = new Subject<void>()

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers()
    this.loadRoles()
    // Subscribe to loading state
    this.userService.loading$.pipe(takeUntil(this.destroy$)).subscribe((loading) => (this.loading = loading))
  }

  ngOnDestroy(): void {
    this.destroy$.next()
    this.destroy$.complete()
  }

  loadUsers(): void {
    this.userService
      .getAllUsers()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (users: UserDto[]) => {
          this.users = users
          this.applyFilters()
          this.clearMessages()
        },
        error: (error: any) => {
          this.error = "Error loading users."
          console.error("Error loading users:", error)
        },
      })
  }

  loadRoles(): void {
    this.userService
      .getAllRoles()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (roles: RoleDto[]) => {
          this.roles = roles
        },
        error: (error: any) => {
          console.error("Error loading roles:", error)
        },
      })
  }

  applyFilters(): void {
    this.filteredUsers = this.users.filter((user) => {
      const matchesSearch =
        !this.searchTerm ||
        user.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(this.searchTerm.toLowerCase())

      const matchesRole = !this.selectedRoleFilter || user.role === this.selectedRoleFilter

      const matchesStatus =
        !this.selectedStatusFilter ||
        (this.selectedStatusFilter === "active" && !user.blocked) ||
        (this.selectedStatusFilter === "blocked" && user.blocked)

      return matchesSearch && matchesRole && matchesStatus
    })
  }

  getRoleClass(role: string): string {
    return "role-" + role.toLowerCase().replace(/\s+/g, "-")
  }

  getInitial(name?: string): string {
    return name?.charAt(0).toUpperCase() || "?"
  }

  onSearchChange(): void {
    this.applyFilters()
  }

  onRoleFilterChange(): void {
    this.applyFilters()
  }

  onStatusFilterChange(): void {
    this.applyFilters()
  }

  // Single user actions
  openRoleModal(user: UserDto): void {
    this.userToUpdateRole = user
    this.newRoleForUser = user.role
    this.showRoleModal = true
  }

  updateUserRole(): void {
    if (!this.userToUpdateRole || !this.newRoleForUser) return

    this.userService
      .updateUserRole(this.userToUpdateRole.id, this.newRoleForUser)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedUser: UserDto) => {
          const index = this.users.findIndex((u) => u.id === updatedUser.id)
          if (index !== -1) {
            this.users[index] = updatedUser
            this.applyFilters()
          }
          this.successMessage = `User ${updatedUser.name} role has been updated successfully.`
          this.closeRoleModal()
          this.autoHideMessage()
        },
        error: (error: any) => {
          this.error = "Error updating user role."
          console.error("Error updating user role:", error)
          this.autoHideMessage()
        },
      })
  }

  toggleUserBlock(user: UserDto): void {
    const observable = user.blocked ? this.userService.unblockUser(user.id) : this.userService.blockUser(user.id)

    observable.pipe(takeUntil(this.destroy$)).subscribe({
      next: (updatedUser: UserDto) => {
        const index = this.users.findIndex((u) => u.id === updatedUser.id)
        if (index !== -1) {
          this.users[index] = updatedUser
          this.applyFilters()
        }
        this.successMessage = `User ${updatedUser.name} has been ${
          user.blocked ? "activated" : "blocked"
        } successfully.`
        this.autoHideMessage()
      },
      error: (error: any) => {
        this.error = `Error ${user.blocked ? "activating" : "blocking"} user.`
        console.error(`Error toggling user block:`, error)
        this.autoHideMessage()
      },
    })
  }

  openDeleteModal(user: UserDto): void {
    this.userToDelete = user
    this.showDeleteModal = true
  }

  deleteUser(): void {
    if (!this.userToDelete) return

    this.userService
      .deleteUser(this.userToDelete.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const userName = this.userToDelete!.name
          // Remove user from local array since we're filtering out deleted users
          this.users = this.users.filter((u) => u.id !== this.userToDelete!.id)
          this.applyFilters()
          this.successMessage = `User ${userName} has been deleted successfully.`
          this.closeDeleteModal()
          this.autoHideMessage()
        },
        error: (error: any) => {
          this.error = "Error deleting user."
          console.error("Error deleting user:", error)
          this.autoHideMessage()
        },
      })
  }

  // Modal methods
  closeRoleModal(): void {
    this.showRoleModal = false
    this.userToUpdateRole = null
    this.newRoleForUser = ""
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false
    this.userToDelete = null
  }

  // Utility methods
  clearMessages(): void {
    this.error = ""
    this.successMessage = ""
  }

  getUserStatusClass(user: UserDto): string {
    if (user.blocked) return "status-blocked"
    return "status-active"
  }

  getUserStatusText(user: UserDto): string {
    if (user.blocked) return "Blocked"
    return "Active"
  }

  refreshUsers(): void {
    this.loadUsers()
  }

  // Auto-hide messages after 5 seconds
  private autoHideMessage(): void {
    setTimeout(() => {
      this.clearMessages()
    }, 5000)
  }
}
