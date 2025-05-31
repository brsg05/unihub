import { Component, OnInit } from '@angular/core';
import { ProfessorService } from '../../../core/services/professor.service';
import { Professor, ProfessorDto } from '../../../models/professor.model'; // Usaremos ProfessorDto para a lista
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { Page } from '../../../models/professor.model'; // Reutilizando Page

// Imports do Angular Material que provavelmente serão usados
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSortModule, Sort } from '@angular/material/sort';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AdminProfessorFormComponent, AdminProfessorFormData } from './admin-professor-form/admin-professor-form.component'; // Importar o formulário

@Component({
  selector: 'app-admin-professores',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatFormFieldModule,
    MatInputModule,
    // AdminProfessorFormComponent // Não precisa importar aqui se for usado apenas em dialog
  ],
  templateUrl: './admin-professores.component.html',
  styleUrls: ['./admin-professores.component.scss']
})
export class AdminProfessoresComponent implements OnInit {
  displayedColumns: string[] = ['id', 'nomeCompleto', 'notaGeral', 'actions'];
  dataSource: ProfessorDto[] = [];
  isLoading = true;
  totalElements = 0;
  pageSize = 10;
  pageIndex = 0;
  sortField = 'nomeCompleto';
  sortDirection: 'asc' | 'desc' = 'asc';

  constructor(
    private professorService: ProfessorService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.loadProfessores();
  }

  loadProfessores(): void {
    this.isLoading = true;
    // Simplificando a chamada para usar apenas paginação.
    // A ordenação via matSort (cliente) e a ordenação via backend (se implementada no serviço) podem ser tratadas separadamente.
    this.professorService.getAllProfessores(this.pageIndex, this.pageSize) // Removidos os parâmetros de sort/filter daqui por enquanto
      .subscribe({
        next: (page: Page<ProfessorDto>) => {
          this.dataSource = page.content;
          this.totalElements = page.totalElements;
          this.isLoading = false;
        },
        error: (err: any) => {
          this.snackBar.open('Erro ao carregar professores.', 'Fechar', { duration: 5000 });
          this.isLoading = false;
          console.error('Erro ao carregar professores:', err);
        }
      });
  }

  handlePageEvent(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProfessores();
  }

  handleSortChange(sort: Sort): void {
    this.sortField = sort.active;
    this.sortDirection = sort.direction as 'asc' | 'desc';
    // Para ordenação no backend, precisaríamos chamar loadProfessores() aqui
    // e o serviço precisaria suportar os parâmetros de sort.
    // Por enquanto, MatSort fará a ordenação no cliente dos dados da página atual.
    // Se quisermos recarregar com ordenação do backend, descomente e ajuste o serviço:
    // this.pageIndex = 0;
    // this.loadProfessores(); 
  }

  openAddProfessorDialog(): void {
    const dialogRef = this.dialog.open<AdminProfessorFormComponent, AdminProfessorFormData, boolean>(
      AdminProfessorFormComponent,
      {
        width: '600px',
        data: { professor: null }, // Novo professor
        disableClose: true // Evitar fechar clicando fora
      }
    );

    dialogRef.afterClosed().subscribe(result => {
      if (result) { // result será true se o formulário foi salvo com sucesso
        this.loadProfessores(); // Recarregar lista
      }
    });
  }

  openEditProfessorDialog(professor: ProfessorDto): void {
    const dialogRef = this.dialog.open<AdminProfessorFormComponent, AdminProfessorFormData, boolean>(
      AdminProfessorFormComponent,
      {
        width: '600px',
        data: { professor: professor }, // Professor existente para edição
        disableClose: true
      }
    );

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadProfessores(); // Recarregar lista
      }
    });
  }

  deleteProfessor(professorId: number): void {
    // Adicionar um diálogo de confirmação aqui (e.g., MatConfirmDialogComponent)
    if (confirm(`Tem certeza que deseja excluir o professor ID ${professorId}? Esta ação não pode ser desfeita.`)) {
      this.isLoading = true;
      this.professorService.deleteProfessor(professorId).subscribe({
        next: () => {
          this.snackBar.open('Professor excluído com sucesso!', 'OK', { duration: 3000 });
          this.loadProfessores(); // Recarregar lista
        },
        error: (err) => {
          this.snackBar.open('Erro ao excluir professor.', 'Fechar', { duration: 5000 });
          this.isLoading = false;
          console.error(err);
        }
      });
    }
  }
} 