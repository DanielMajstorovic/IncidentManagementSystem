import type { Routes } from "@angular/router"
import { MainMapComponent } from "./views/main-map/main-map.component"
import { UserMapComponent } from "./views/user-map/user-map.component"
import { AuthGuard } from "./core/guards/auth.guard"
import { ModeratorIncidentsComponent } from "./views/moderator-incidents/moderator-incidents.component"
import { ModeratorAnalyticsComponent } from "./views/moderator-analytics/moderator-analytics.component"
import { AlertParamsComponent } from "./views/alert-params/alert-params.component"
import { AdminIncidentsComponent } from "./views/admin-incidents/admin-incidents.component"
import { UserManagementComponent } from "./views/user-management/user-management.component"
import { UnauthorizedComponent } from "./views/unauthorized/unauthorized.component"
import { AuthErrorComponent } from "./views/auth-error/auth-error.component"
import { RoleManagementComponent } from "./views/role-management/role-management.component"

export const routes: Routes = [
  {
    path: "",
    component: MainMapComponent,
    title: "Incident Reporting System",
  },
  {
    path: "login-error",
    component: AuthErrorComponent,
    title: "Authentication Error",
  },
  {
    path: "user-map",
    component: UserMapComponent,
    canActivate: [AuthGuard],
    data: { roles: ["USER", "MODERATOR", "ADMIN"] },
    title: "User Map",
  },
  {
    path: "moderator/incidents",
    component: ModeratorIncidentsComponent,
    canActivate: [AuthGuard],
    data: { roles: ["MODERATOR", "ADMIN"] },
    title: "Moderator - Incidents",
  },
  {
    path: "moderator/analytics",
    component: ModeratorAnalyticsComponent,
    canActivate: [AuthGuard],
    data: { roles: ["MODERATOR", "ADMIN"] },
    title: "Moderator - Analytics",
  },
  {
    path: "moderator/alert-params",
    component: AlertParamsComponent,
    canActivate: [AuthGuard],
    data: { roles: ["MODERATOR", "ADMIN"] },
    title: "Alert Parameters",
  },
  {
    path: "admin/incidents",
    component: AdminIncidentsComponent,
    canActivate: [AuthGuard],
    data: { roles: ["ADMIN"] },
    title: "Admin - Incidents",
  },
  {
    path: "admin/user-management",
    component: UserManagementComponent,
    canActivate: [AuthGuard],
    data: { roles: ["ADMIN"] },
    title: "User Management",
  },
  {
    path: "admin/role-management",
    component: RoleManagementComponent,
    canActivate: [AuthGuard],
    data: { roles: ["ADMIN"] },
    title: "Role Management",
  },
  {
    path: "unauthorized",
    component: UnauthorizedComponent,
    title: "Access Denied",
  },
  {
    path: "**",
    redirectTo: "",
  },
]
