import { Routes } from '@angular/router';
import { MainMapComponent } from './views/main-map/main-map.component';

export const routes: Routes = [
  {
    path: '',
    component: MainMapComponent,
    title: 'Incident Reporting System'
  },
  {
    path: '**',
    redirectTo: ''
  }
];