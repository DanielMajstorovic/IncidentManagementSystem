import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import {
  RoleDto,
  UserDto,
  UserService,
} from '../../core/services/user.service';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss'],
})
export class UserManagementComponent implements OnInit, OnDestroy {
  users: UserDto[] = [];
  roles: RoleDto[] = [];
  filteredUsers: UserDto[] = [];

  // Search and filter
  searchTerm = '';
  selectedRoleFilter = '';
  selectedStatusFilter = '';

  // Selection
  selectedUsers = new Set<number>();
  selectAll = false;

  // Modal states
  showDeleteModal = false;
  showRoleModal = false;
  userToDelete: UserDto | null = null;
  userToUpdateRole: UserDto | null = null;
  newRoleForUser = '';

  // Loading and error states
  loading = false;
  error = '';
  successMessage = '';

  private destroy$ = new Subject<void>();

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadRoles();

    // Subscribe to loading state
    this.userService.loading$
      .pipe(takeUntil(this.destroy$))
      .subscribe((loading) => (this.loading = loading));
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadUsers(): void {
    this.userService
      .getAllUsers()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (users: UserDto[]) => {
          this.users = users;
          this.applyFilters();
          this.clearMessages();
        },
        error: (error: any) => {
          this.error = 'Error loading users.';
          console.error('Error loading users:', error);
        },
      });
  }

  loadRoles(): void {
    this.userService
      .getAllRoles()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (roles: RoleDto[]) => {
          this.roles = roles;
        },
        error: (error: any) => {
          console.error('Error loading roles:', error);
        },
      });
  }

  applyFilters(): void {
    this.filteredUsers = this.users.filter((user) => {
      const matchesSearch =
        !this.searchTerm ||
        user.name.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(this.searchTerm.toLowerCase());

      const matchesRole =
        !this.selectedRoleFilter || user.role === this.selectedRoleFilter;

      const matchesStatus =
        !this.selectedStatusFilter ||
        (this.selectedStatusFilter === 'active' &&
          !user.blocked &&
          !user.deleted) ||
        (this.selectedStatusFilter === 'blocked' && user.blocked) ||
        (this.selectedStatusFilter === 'deleted' && user.deleted);

      return matchesSearch && matchesRole && matchesStatus;
    });
    this.updateSelectAllState();
  }

  getRoleClass(role: string): string {
    return 'role-' + role.toLowerCase().replace(/\s+/g, '-');
  }

  getInitial(name?: string): string {
    return name?.charAt(0).toUpperCase() || '?';
  }

  onSearchChange(): void {
    this.applyFilters();
    this.clearSelection();
  }

  onRoleFilterChange(): void {
    this.applyFilters();
    this.clearSelection();
  }

  onStatusFilterChange(): void {
    this.applyFilters();
    this.clearSelection();
  }

  // Selection methods
  toggleSelectAll(): void {
    if (this.selectAll) {
      this.selectedUsers.clear();
    } else {
      this.filteredUsers.forEach((user) => this.selectedUsers.add(user.id));
    }
    this.selectAll = !this.selectAll;
  }

  toggleUserSelection(userId: number): void {
    if (this.selectedUsers.has(userId)) {
      this.selectedUsers.delete(userId);
    } else {
      this.selectedUsers.add(userId);
    }
    this.updateSelectAllState();
  }

  isUserSelected(userId: number): boolean {
    return this.selectedUsers.has(userId);
  }

  updateSelectAllState(): void {
    this.selectAll =
      this.filteredUsers.length > 0 &&
      this.filteredUsers.every((user) => this.selectedUsers.has(user.id));
  }

  clearSelection(): void {
    this.selectedUsers.clear();
    this.selectAll = false;
  }

  // Single user actions
  openRoleModal(user: UserDto): void {
    this.userToUpdateRole = user;
    this.newRoleForUser = user.role;
    this.showRoleModal = true;
  }

  updateUserRole(): void {
    if (!this.userToUpdateRole || !this.newRoleForUser) return;

    this.userService
      .updateUserRole(this.userToUpdateRole.id, this.newRoleForUser)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (updatedUser: UserDto) => {
          const index = this.users.findIndex((u) => u.id === updatedUser.id);
          if (index !== -1) {
            this.users[index] = updatedUser;
            this.applyFilters();
          }
          this.successMessage = `User ${updatedUser.name} role has been updated successfully.`;
          this.closeRoleModal();
        },
        error: (error: any) => {
          this.error = 'Error updating user role.';
          console.error('Error updating user role:', error);
        },
      });
  }

  toggleUserBlock(user: UserDto): void {
    const action = user.blocked ? 'unblock' : 'block';
    const observable = user.blocked
      ? this.userService.unblockUser(user.id)
      : this.userService.blockUser(user.id);

    observable.pipe(takeUntil(this.destroy$)).subscribe({
      next: (updatedUser: UserDto) => {
        const index = this.users.findIndex((u) => u.id === updatedUser.id);
        if (index !== -1) {
          this.users[index] = updatedUser;
          this.applyFilters();
        }
        this.successMessage = `User ${updatedUser.name} has been ${
          user.blocked ? 'activated' : 'blocked'
        } successfully.`;
      },
      error: (error: any) => {
        this.error = `Error ${user.blocked ? 'activating' : 'blocking'} user.`;
        console.error(`Error ${action} user:`, error);
      },
    });
  }

  openDeleteModal(user: UserDto): void {
    this.userToDelete = user;
    this.showDeleteModal = true;
  }

  deleteUser(): void {
    if (!this.userToDelete) return;

    this.userService
      .deleteUser(this.userToDelete.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          const userName = this.userToDelete!.name;
          // Update user in local array (mark as deleted)
          const index = this.users.findIndex(
            (u) => u.id === this.userToDelete!.id
          );
          if (index !== -1) {
            this.users[index].deleted = true;
            this.applyFilters();
          }
          this.successMessage = `User ${userName} has been deleted successfully.`;
          this.closeDeleteModal();
        },
        error: (error: any) => {
          this.error = 'Error deleting user.';
          console.error('Error deleting user:', error);
        },
      });
  }

  // Bulk actions
  bulkBlockUsers(): void {
    const userIds = Array.from(this.selectedUsers);
    if (userIds.length === 0) return;

    this.userService
      .bulkBlockUsers(userIds)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (results: any[]) => {
          const successCount = results.filter((r) => r.success).length;
          this.successMessage = `${successCount} users have been blocked successfully.`;
          this.loadUsers();
          this.clearSelection();
        },
        error: (error: any) => {
          this.error = 'Error blocking users in bulk.';
          console.error('Error bulk blocking users:', error);
        },
      });
  }

  bulkUnblockUsers(): void {
    const userIds = Array.from(this.selectedUsers);
    if (userIds.length === 0) return;

    this.userService
      .bulkUnblockUsers(userIds)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (results: any[]) => {
          const successCount = results.filter((r) => r.success).length;
          this.successMessage = `${successCount} users have been activated successfully.`;
          this.loadUsers();
          this.clearSelection();
        },
        error: (error: any) => {
          this.error = 'Error activating users in bulk.';
          console.error('Error bulk unblocking users:', error);
        },
      });
  }

  // Modal methods
  closeRoleModal(): void {
    this.showRoleModal = false;
    this.userToUpdateRole = null;
    this.newRoleForUser = '';
  }

  closeDeleteModal(): void {
    this.showDeleteModal = false;
    this.userToDelete = null;
  }

  // Utility methods
  clearMessages(): void {
    this.error = '';
    this.successMessage = '';
  }

  getUserStatusClass(user: UserDto): string {
    if (user.deleted) return 'status-deleted';
    if (user.blocked) return 'status-blocked';
    return 'status-active';
  }

  getUserStatusText(user: UserDto): string {
    if (user.deleted) return 'Deleted';
    if (user.blocked) return 'Blocked';
    return 'Active';
  }

  getSelectedUsersCount(): number {
    return this.selectedUsers.size;
  }

  canPerformBulkActions(): boolean {
    return this.getSelectedUsersCount() > 0;
  }

  refreshUsers(): void {
    this.clearSelection();
    this.loadUsers();
  }

  // Auto-hide messages after 5 seconds
  private autoHideMessage(): void {
    setTimeout(() => {
      this.clearMessages();
    }, 5000);
  }
}
