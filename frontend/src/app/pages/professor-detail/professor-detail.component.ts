import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProfessorService } from '../../core/services/professor.service';
import { ProfessorDetailDto } from '../../models/professor.model';
import { Cadeira } from '../../models/cadeira.model';
import { Subscription } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { SelecionarCadeiraDialogComponent, SelecionarCadeiraDialogData } from './selecionar-cadeira-dialog/selecionar-cadeira-dialog.component';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-professor-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatListModule,
    MatTableModule,
    MatIconModule,
    MatDividerModule,
    MatButtonModule,
    MatDialogModule,
    RouterLink
  ],
  templateUrl: './professor-detail.component.html',
  styleUrls: ['./professor-detail.component.scss']
})
export class ProfessorDetailComponent implements OnInit, OnDestroy {
  professor: ProfessorDetailDto | null = null;
  isLoading = true;
  errorMessage: string | null = null;
  private routeSub: Subscription | undefined;
  private professorSub: Subscription | undefined;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private professorService: ProfessorService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) { }

  ngOnInit(): void {
    this.routeSub = this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.fetchProfessorDetails(+id);
      } else {
        this.isLoading = false;
        this.errorMessage = 'ID do professor não encontrado na URL.';
        this.snackBar.open(this.errorMessage, 'Fechar', { duration: 5000 });
      }
    });
  }

  fetchProfessorDetails(id: number): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.professorSub = this.professorService.getProfessorById(id).subscribe({
      next: (data: ProfessorDetailDto) => {
        this.professor = data;
        this.isLoading = false;
      },
      error: (error: HttpErrorResponse) => {
        this.isLoading = false;
        this.errorMessage = 'Erro ao buscar detalhes do professor.';
        if (error.status === 404) {
          this.errorMessage = 'Professor não encontrado.';
        }
        this.snackBar.open(this.errorMessage, 'Fechar', { duration: 5000 });
        console.error('Error fetching professor details:', error);
      }
    });
  }

  iniciarAvaliacao(): void {
    if (!this.professor) {
      this.snackBar.open('Professor não carregado.', 'Fechar', { duration: 3000 });
      return;
    }

    const cadeiras = this.professor.cadeiras;

    if (!cadeiras || cadeiras.length === 0) {
      this.snackBar.open('Este professor não possui cadeiras para avaliação.', 'Fechar', { duration: 3000 });
      return;
    }

    if (cadeiras.length === 1) {
      this.prosseguirParaAvaliacao(cadeiras[0]);
    } else {
      const dialogRef = this.dialog.open<SelecionarCadeiraDialogComponent, SelecionarCadeiraDialogData, { selectedCadeira?: Cadeira }>(
        SelecionarCadeiraDialogComponent,
        {
          width: '400px',
          data: { cadeiras: cadeiras }
        }
      );

      dialogRef.afterClosed().subscribe(result => {
        if (result && result.selectedCadeira) {
          this.prosseguirParaAvaliacao(result.selectedCadeira);
        }
      });
    }
  }

  prosseguirParaAvaliacao(cadeira: Cadeira): void {
    if (!this.professor) return;

    console.log(`Prosseguindo para avaliação do professor ${this.professor.nomeCompleto} (ID: ${this.professor.id}) na cadeira ${cadeira.nome} (ID: ${cadeira.id})`);
    this.router.navigate([
      '/avaliar/professor', 
      this.professor.id, 
      'cadeira', 
      cadeira.id
    ], {
      state: { cadeiraNome: cadeira.nome }
    });
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    this.professorSub?.unsubscribe();
  }
} 