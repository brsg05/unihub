import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTabsModule } from '@angular/material/tabs';
import { AdminCriteriosComponent } from '../admin-criterios/admin-criterios.component';
import { AdminProfessoresComponent } from '../admin-professores/admin-professores.component';
import { AdminCadeirasComponent } from '../admin-cadeiras/admin-cadeiras.component';
import { AdminUsersComponent } from '../admin-users/admin-users.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatTabsModule,
    AdminCriteriosComponent,
    AdminProfessoresComponent,
    AdminCadeirasComponent,
    AdminUsersComponent
  ],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

} 