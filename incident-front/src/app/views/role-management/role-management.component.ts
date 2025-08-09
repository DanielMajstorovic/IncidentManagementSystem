import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { EndpointDto, RoleDetailDto, RoleDto, UserDto, UserService } from '../../core/services/user.service';
import { RoleService } from '../../core/services/role.service';

@Component({
  selector: 'app-role-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './role-management.component.html',
  styleUrls: ['./role-management.component.scss']
})
export class RoleManagementComponent implements OnInit, OnDestroy {
  // Data stores
  roles: RoleDetailDto[] = [];
  allEndpoints: EndpointDto[] = [];
  users: UserDto[] = [];
  filteredRoles: RoleDetailDto[] = [];

  // Search and filter
  searchTerm = '';

  // Modal states
  showCreateModal = false;
  showPermissionsModal = false;
  showEndpointsModal = false;
  showDeleteModal = false;
  
  // Data for modals
  newRoleName = '';
  roleToEdit: RoleDetailDto | null = null;
  roleToDelete: RoleDetailDto | null = null;
  permissionSelection: { [key: number]: boolean } = {};

  // Loading and messaging
  loading = false;
  error = '';
  successMessage = '';

  private destroy$ = new Subject<void>();

  constructor(
    private roleService: RoleService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.loadInitialData();
    this.roleService.loading$.pipe(takeUntil(this.destroy$)).subscribe((loading: any) => this.loading = loading);
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadInitialData(): void {
    this.error = '';
    this.roleService.loading$.pipe(takeUntil(this.destroy$)).subscribe((loading: any) => this.loading = loading);
    
    forkJoin({
      roles: this.roleService.getAllRoles(),
      users: this.userService.getAllUsers(),
      endpoints: this.roleService.getAllEndpoints()
    }).pipe(takeUntil(this.destroy$)).subscribe({
      next: ({ roles, users, endpoints }) => {
        this.users = users;
        this.allEndpoints = endpoints;
        this.roles = this.calculateUserCounts(roles);
        this.applyFilters();
      },
      error: err => {
        this.error = 'Failed to load initial data. Please try again.';
        console.error(err);
        this.autoHideMessage();
      }
    });
  }

  calculateUserCounts(roles: RoleDto[]): RoleDetailDto[] {
    const userCounts = this.users.reduce((acc, user) => {
      acc[user.role] = (acc[user.role] || 0) + 1;
      return acc;
    }, {} as { [key: string]: number });

    return roles.map(role => ({
      ...role,
      userCount: userCounts[role.name] || 0,
      endpointIds: (role as any).endpointIds || []
    }));
  }

  applyFilters(): void {
    this.filteredRoles = this.roles.filter(role =>
      !this.searchTerm || role.name.toLowerCase().includes(this.searchTerm.toLowerCase())
    );
  }

  refreshData(): void {
    this.loadInitialData();
    this.successMessage = 'Data has been refreshed.';
    this.autoHideMessage();
  }

  // Create Role Modal
  openCreateModal(): void {
    this.newRoleName = '';
    this.showCreateModal = true;
  }
  
  closeCreateModal(): void { 
    this.showCreateModal = false; 
  }
  
  createRole(): void {
    if (!this.newRoleName.trim()) return;
    this.roleService.createRole(this.newRoleName.trim()).subscribe({
      next: () => {
        this.successMessage = `Role '${this.newRoleName}' created successfully.`;
        this.loadInitialData();
        this.closeCreateModal();
        this.autoHideMessage();
      },
      error: (err: any) => {
        this.error = 'Failed to create role.';
        console.error(err);
        this.autoHideMessage();
      }
    });
  }

  // Permissions Modal
  openPermissionsModal(role: RoleDetailDto): void {
    this.roleToEdit = role;
    this.permissionSelection = this.allEndpoints.reduce((acc, endpoint) => {
      acc[endpoint.id] = role.endpointIds.includes(endpoint.id);
      return acc;
    }, {} as { [key: number]: boolean });
    this.showPermissionsModal = true;
  }
  
  closePermissionsModal(): void { 
    this.showPermissionsModal = false; 
  }
  
  updatePermissions(): void {
    if (!this.roleToEdit) return;
    const selectedEndpointIds = Object.keys(this.permissionSelection)
      .filter(id => this.permissionSelection[+id])
      .map(id => +id);
    
    this.roleService.updateRolePermissions(this.roleToEdit.id, selectedEndpointIds).subscribe({
      next: () => {
        this.successMessage = `Permissions for role '${this.roleToEdit?.name}' updated.`;
        this.closePermissionsModal();
        this.loadInitialData();
        this.autoHideMessage();
      },
      error: (err: any) => {
        this.closePermissionsModal();
        this.error = 'Failed to update permissions.';
        console.error(err);
        this.autoHideMessage();
      }
    });
  }

  // Endpoints Modal
  openEndpointsModal(): void { 
    this.showEndpointsModal = true; 
  }
  
  closeEndpointsModal(): void { 
    this.showEndpointsModal = false; 
  }

  // Delete Role Modal
  openDeleteModal(role: RoleDetailDto): void {
    if (role.userCount > 0) return;
    this.roleToDelete = role;
    this.showDeleteModal = true;
  }
  
  closeDeleteModal(): void { 
    this.showDeleteModal = false; 
  }
  
  deleteRole(): void {
    if (!this.roleToDelete) return;
    this.roleService.deleteRole(this.roleToDelete.id).subscribe({
      next: () => {
        this.successMessage = `Role '${this.roleToDelete?.name}' deleted successfully.`;
        this.loadInitialData();
        this.closeDeleteModal();
        this.autoHideMessage();
      },
      error: (err: any) => {
        this.closeDeleteModal();
        this.error = 'Failed to delete role. It might be in use.';
        console.error(err);
        this.autoHideMessage();
      }
    });
  }

  getRoleClass(role: string): string {
    if (!role) {
      return 'role-default';
    }
    const formattedRole = role.toLowerCase().replace(/\s+/g, "-");
    return `role-${formattedRole}`;
  }

  getSelectedCount(): number {
    return Object.values(this.permissionSelection).filter(selected => selected).length;
  }

  getMethodClass(method: string): string {
    return `method-${method.toLowerCase()}`;
  }

  getEndpointsByMethod(method: string): EndpointDto[] {
    return this.allEndpoints.filter(endpoint => endpoint.httpMethod === method);
  }
  
  // Utility methods
  getInitial(name?: string): string { 
    return name?.charAt(0).toUpperCase() || "?"; 
  }
  
  clearMessages(): void { 
    this.error = ""; 
    this.successMessage = ""; 
  }
  
  private autoHideMessage(): void { 
    setTimeout(() => this.clearMessages(), 5000); 
  }
}