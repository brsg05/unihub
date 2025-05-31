import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';

import { CriterioService } from '../../../core/services/criterio.service';
import { Criterio } from '../../../models/criterio.model';
import { Page } from '../../../models/professor.model'; // Reusing Page
// TODO: Create and import AdminCriterioDialogComponent for create/edit
import { AdminCriterioDialogComponent, AdminCriterioDialogData } from './admin-criterio-dialog/admin-criterio-dialog.component'; // Import dialog component
// import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component'; // Commented out for now

import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialogModule } from '@angular/material/dialog';

@Component({
  selector: 'app-admin-criterios',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatDialogModule,
    AdminCriterioDialogComponent, // Add AdminCriterioDialogComponent to imports if it's standalone
    // ConfirmDialogComponent might need to be imported if standalone
  ],
  templateUrl: './admin-criterios.component.html',
  styleUrls: ['./admin-criterios.component.scss']
})
export class AdminCriteriosComponent implements OnInit, OnDestroy {
  displayedColumns: string[] = ['id', 'nome', 'actions'];
  dataSource: MatTableDataSource<Criterio> = new MatTableDataSource();
  isLoading = true;
  totalElements = 0;
  pageSize = 10;
  currentPage = 0;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private criteriosSub: Subscription | undefined;

  constructor(
    private criterioService: CriterioService,
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCriterios();
  }

  ngOnDestroy(): void {
    this.criteriosSub?.unsubscribe();
  }

  loadCriterios(): void {
    this.isLoading = true;
    this.criteriosSub = this.criterioService.getAllCriterios(this.currentPage, this.pageSize)
      .subscribe({
        next: (data: Page<Criterio>) => {
          this.dataSource.data = data.content;
          this.totalElements = data.totalElements;
          this.isLoading = false;
          // No need to connect paginator here if server-side pagination
        },
        error: (err) => {
          this.snackBar.open('Erro ao carregar critérios.', 'Fechar', { duration: 3000 });
          this.isLoading = false;
          console.error(err);
        }
      });
  }

  pageChanged(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadCriterios();
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    // Client-side filtering, for server-side, this would trigger an API call
    this.dataSource.filter = filterValue.trim().toLowerCase(); 
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open<AdminCriterioDialogComponent, AdminCriterioDialogData, Partial<Criterio>>(AdminCriterioDialogComponent, {
      width: '400px',
      data: { criterio: null, isEditMode: false } // Pass null for creation mode
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result && result.nome) { // Result would be the Criterio data from the form
        this.criterioService.createCriterio({ nome: result.nome }).subscribe({
          next: () => {
            this.snackBar.open('Critério criado com sucesso!', 'Fechar', { duration: 3000 });
            this.loadCriterios(); // Refresh list
          },
          error: (err) => {
            console.error(err);
            this.snackBar.open('Erro ao criar critério: ' + (err.error?.message || err.message), 'Fechar', { duration: 5000 });
          }
        });
      }
    });
    // this.snackBar.open('Funcionalidade de Criar Critério a ser implementada com Dialog.', 'Fechar', { duration: 3000 });
  }

  openEditDialog(criterio: Criterio): void {
    const dialogRef = this.dialog.open<AdminCriterioDialogComponent, AdminCriterioDialogData, Partial<Criterio>>(AdminCriterioDialogComponent, {
      width: '400px',
      data: { criterio: { ...criterio }, isEditMode: true } // Pass existing criterio for editing
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result && result.nome) { // Result would be the updated Criterio data
        this.criterioService.updateCriterio(criterio.id, { nome: result.nome }).subscribe({ // Pass only name for update as per CriterioService
          next: () => {
            this.snackBar.open('Critério atualizado com sucesso!', 'Fechar', { duration: 3000 });
            this.loadCriterios(); // Refresh list
          },
          error: (err) => {
            console.error(err);
            this.snackBar.open('Erro ao atualizar critério: ' + (err.error?.message || err.message), 'Fechar', { duration: 5000 });
          }
        });
      }
    });
    // this.snackBar.open(`Editar critério ID: ${criterio.id} (a ser implementado com Dialog).`, 'Fechar', { duration: 3000 });
  }

  deleteCriterio(id: number): void {
    // const dialogRef = this.dialog.open(ConfirmDialogComponent, { // Usage also commented
    //   width: '350px',
    //   data: { title: 'Confirmar Exclusão', message: 'Tem certeza que deseja excluir este critério?' }
    // });
    // dialogRef.afterClosed().subscribe(result => {
    //   if (result) { // result is true if confirmed
    //     this.criterioService.deleteCriterio(id).subscribe({
    //       next: () => {
    //         this.snackBar.open('Critério excluído com sucesso!', 'Fechar', { duration: 3000 });
    //         this.loadCriterios(); // Refresh list
    //       },
    //       error: (err) => this.snackBar.open('Erro ao excluir critério.', 'Fechar', { duration: 3000 })
    //     });
    //   }
    // });
     this.snackBar.open(`Excluir critério ID: ${id} (a ser implementado com Dialog de confirmação).`, 'Fechar', { duration: 3000 });
  }
} 