import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatTableDataSource } from '@angular/material/table';
import { Curso } from '../../../models/curso.model';
import { CursoService } from '../../../services/curso.service';
import { AdminCursoDialogComponent, AdminCursoDialogData } from './admin-curso-dialog/admin-curso-dialog.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, finalize, tap } from 'rxjs/operators';
import { of } from 'rxjs';

@Component({
  selector: 'app-admin-cursos',
  templateUrl: './admin-cursos.component.html',
  styleUrls: ['./admin-cursos.component.scss', '../../admin-shared.scss']
})
export class AdminCursosComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = ['id', 'nome', 'acoes'];
  dataSource: MatTableDataSource<Curso> = new MatTableDataSource();
  cursos: Curso[] = [];
  isLoading = true;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private cursoService: CursoService,
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCursos();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  loadCursos(): void {
    this.isLoading = true;
    this.cursoService.getAllCursos().pipe(
      tap(data => {
        this.cursos = data;
        this.dataSource.data = this.cursos;
      }),
      catchError(error => {
        console.error('Error loading cursos:', error);
        this.snackBar.open('Erro ao carregar cursos.', 'Fechar', { duration: 3000 });
        return of([]);
      }),
      finalize(() => this.isLoading = false)
    ).subscribe();
  }

  openDialog(curso?: Curso): void {
    const dialogData: AdminCursoDialogData = {
      isEdit: !!curso,
      curso: curso ? { ...curso } : undefined // Pass a copy for editing
    };

    const dialogRef = this.dialog.open(AdminCursoDialogComponent, {
      width: '400px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result && result.nome) { // Ensure result has nome, as dialog only returns {nome: string}
        this.isLoading = true;
        const cursoData = { nome: result.nome };
        if (dialogData.isEdit && dialogData.curso) {
          this.cursoService.updateCurso(dialogData.curso.id, cursoData).pipe(
            tap(() => {
              this.snackBar.open('Curso atualizado com sucesso!', 'Fechar', { duration: 3000 });
              this.loadCursos();
            }),
            catchError(err => {
              console.error("Error updating curso: ", err);
              this.snackBar.open(err.error?.message || 'Erro ao atualizar curso.', 'Fechar', { duration: 3000 });
              this.isLoading = false;
              return of(null);
            })
          ).subscribe();
        } else {
          this.cursoService.createCurso(cursoData).pipe(
            tap(() => {
              this.snackBar.open('Curso adicionado com sucesso!', 'Fechar', { duration: 3000 });
              this.loadCursos();
            }),
            catchError(err => {
              console.error("Error creating curso: ", err);
              this.snackBar.open(err.error?.message || 'Erro ao criar curso.', 'Fechar', { duration: 3000 });
              this.isLoading = false;
              return of(null);
            })
          ).subscribe();
        }
      }
    });
  }

  deleteCurso(id: number, nome: string): void {
    if (confirm(`Tem certeza que deseja excluir o curso "${nome}"? Esta ação não pode ser desfeita e pode falhar se houver cadeiras associadas.`)) {
      this.isLoading = true;
      this.cursoService.deleteCurso(id).pipe(
        tap(() => {
          this.snackBar.open('Curso excluído com sucesso!', 'Fechar', { duration: 3000 });
          this.loadCursos();
        }),
        catchError(err => {
          console.error('Error deleting curso:', err);
          this.snackBar.open(err.error?.message || 'Erro ao excluir curso. Verifique se existem cadeiras associadas a ele.', 'Fechar', { duration: 5000 });
          this.isLoading = false;
          return of(null);
        }),
        finalize(() => this.isLoading = false)
      ).subscribe();
    }
  }
} 