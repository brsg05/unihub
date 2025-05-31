import { Component, OnInit, ViewChild, OnDestroy } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subscription } from 'rxjs';

import { CadeiraService } from '../../../core/services/cadeira.service'; // Updated service
import { Cadeira } from '../../../models/cadeira.model'; // Updated model
import { Page } from '../../../models/professor.model'; // Reusing Page from professor.model
import { AdminCadeiraDialogComponent, AdminCadeiraDialogData } from './admin-cadeira-dialog/admin-cadeira-dialog.component'; // Import dialog component
// import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';

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
  selector: 'app-admin-cadeiras', // Updated selector
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
    AdminCadeiraDialogComponent, // Add AdminCadeiraDialogComponent to imports
    // ConfirmDialogComponent might need to be imported if standalone
  ],
  templateUrl: './admin-cadeiras.component.html', // Updated template URL
  styleUrls: ['./admin-cadeiras.component.scss'] // Updated style URL
})
export class AdminCadeirasComponent implements OnInit, OnDestroy {
  displayedColumns: string[] = ['id', 'nome', 'actions']; // Adjusted columns for Cadeira
  dataSource: MatTableDataSource<Cadeira> = new MatTableDataSource();
  isLoading = true;
  totalElements = 0;
  pageSize = 10;
  currentPage = 0;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  private cadeirasSub: Subscription | undefined;

  constructor(
    private cadeiraService: CadeiraService, // Updated service
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCadeiras();
  }

  ngOnDestroy(): void {
    this.cadeirasSub?.unsubscribe();
  }

  loadCadeiras(): void {
    this.isLoading = true;
    this.cadeirasSub = this.cadeiraService.getAllCadeiras(this.currentPage, this.pageSize) // Service method might differ
      .subscribe({
        next: (data: Page<Cadeira>) => {
          this.dataSource.data = data.content;
          this.totalElements = data.totalElements;
          this.isLoading = false;
        },
        error: (err) => {
          this.snackBar.open('Erro ao carregar cadeiras.', 'Fechar', { duration: 3000 });
          this.isLoading = false;
          console.error(err);
        }
      });
  }

  pageChanged(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadCadeiras();
  }

  applyFilter(event: Event): void {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  openCreateDialog(): void {
    const dialogRef = this.dialog.open<AdminCadeiraDialogComponent, AdminCadeiraDialogData, Partial<Cadeira>>(AdminCadeiraDialogComponent, {
      width: '400px',
      data: { cadeira: null, isEditMode: false }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) { // result contains nome, cargaHoraria, isEletiva
        this.cadeiraService.createCadeira(result).subscribe({ 
          next: () => {
            this.snackBar.open('Cadeira criada com sucesso!', 'Fechar', { duration: 3000 });
            this.loadCadeiras(); // Refresh list
          },
          error: (err) => {
            console.error(err);
            this.snackBar.open('Erro ao criar cadeira: ' + (err.error?.message || err.message), 'Fechar', { duration: 5000 });
          }
        });
      }
    });
  }

  openEditDialog(cadeira: Cadeira): void {
    const dialogRef = this.dialog.open<AdminCadeiraDialogComponent, AdminCadeiraDialogData, Partial<Cadeira>>(AdminCadeiraDialogComponent, {
      width: '400px',
      data: { cadeira: { ...cadeira }, isEditMode: true } // Pass existing cadeira for editing
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result && result.id) { // For update, result should contain id, nome, cargaHoraria, isEletiva
        this.cadeiraService.updateCadeira(result.id, result as Cadeira).subscribe({ 
          next: () => {
            this.snackBar.open('Cadeira atualizada com sucesso!', 'Fechar', { duration: 3000 });
            this.loadCadeiras(); // Refresh list
          },
          error: (err) => {
            console.error(err);
            this.snackBar.open('Erro ao atualizar cadeira: ' + (err.error?.message || err.message), 'Fechar', { duration: 5000 });
          }
        });
      }
    });
  }

  deleteCadeira(id: number): void {
    // For now, directly call delete. Later, implement ConfirmDialogComponent.
    // This is a placeholder for a confirmation dialog.
    const confirmDelete = confirm('Tem certeza que deseja excluir esta cadeira?'); 
    if (confirmDelete) {
      this.cadeiraService.deleteCadeira(id).subscribe({
        next: () => {
          this.snackBar.open('Cadeira excluída com sucesso!', 'Fechar', { duration: 3000 });
          this.loadCadeiras(); // Refresh list
        },
        error: (err) => {
          console.error(err);
          this.snackBar.open('Erro ao excluir cadeira: ' + (err.error?.message || err.message), 'Fechar', { duration: 5000 });
        }
      });
    } 
    // TODO: Replace confirm with MatDialog opening ConfirmDialogComponent
    // const dialogRef = this.dialog.open(ConfirmDialogComponent, {
    //   width: '350px',
    //   data: { title: 'Confirmar Exclusão', message: 'Tem certeza que deseja excluir esta cadeira?' }
    // });
    // dialogRef.afterClosed().subscribe(result => {
    //   if (result) { // result is true if confirmed
    //     this.cadeiraService.deleteCadeira(id).subscribe({
    //       next: () => {
    //         this.snackBar.open('Cadeira excluída com sucesso!', 'Fechar', { duration: 3000 });
    //         this.loadCadeiras(); // Refresh list
    //       },
    //       error: (err) => this.snackBar.open('Erro ao excluir cadeira.', 'Fechar', { duration: 3000 })
    //     });
    //   }
    // });
  }
} 