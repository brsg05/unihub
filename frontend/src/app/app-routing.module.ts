import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home.component';
import { ProfessorListComponent } from './pages/professor-list/professor-list.component';
import { ProfessorDetailComponent } from './pages/professor-detail/professor-detail.component';
import { CriterionDetailComponent } from './pages/criterion-detail/criterion-detail.component';
import { LoginComponent } from './pages/auth/login/login.component';
import { RegisterComponent } from './pages/auth/register/register.component';
import { AdminDashboardComponent } from './pages/admin/admin-dashboard/admin-dashboard.component';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';
import { AvaliacaoFormComponent } from './pages/avaliacao-form/avaliacao-form.component';
import { CriterionEvaluationPageComponent } from './pages/criterion-evaluation-page/criterion-evaluation-page.component';
import { CadeiraListComponent } from './pages/cadeira-list/cadeira-list.component';
import { CadeiraDetailComponent } from './pages/cadeira-detail/cadeira-detail.component';
import { ProfessorCadeiraComentariosComponent } from './pages/professor-cadeira-comentarios/professor-cadeira-comentarios.component';
import { ProfessorCriterioComentariosComponent } from './pages/professor-criterio-comentarios/professor-criterio-comentarios.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'professores', component: ProfessorListComponent },
  { path: 'professor-list', component: ProfessorListComponent },
  { path: 'cadeiras', component: CadeiraListComponent },
  { path: 'cadeira/:id', component: CadeiraDetailComponent },
  { path: 'professor/:id', component: ProfessorDetailComponent },
  { path: 'professor/:profId/criterio/:critId', component: CriterionDetailComponent }, // Simplified route
  { path: 'professor/:professorId/cadeira/:cadeiraId/comentarios', component: ProfessorCadeiraComentariosComponent },
  { path: 'professor/:professorId/criterio/:criterioId/comentarios', component: ProfessorCriterioComentariosComponent },
  { path: 'avaliar/professor/:professorId/cadeira/:cadeiraId', component: AvaliacaoFormComponent, canActivate: [AuthGuard] },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'admin',
    component: AdminDashboardComponent,
    canActivate: [AuthGuard, AdminGuard] // Example of route protection
  },
  {
    path: 'professores/:professorId/criterios/:criterioId/avaliacoes',
    component: CriterionEvaluationPageComponent,
    canActivate: [AuthGuard]
  },
  // Add more routes as needed, e.g., for admin CRUD operations for entities
  { path: '**', redirectTo: '' } // Wildcard route for a 404 page or redirect to home
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { } 