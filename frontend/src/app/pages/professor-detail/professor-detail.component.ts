import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProfessorService } from '../../services/professor.service';
import { ComentarioService } from '../../core/services/comentario.service';
import { ProfessorDetailDto } from '../../models/professor.model';
import { CadeiraSimplificada } from '../../models/cadeira.model';
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
import { CadeiraSelectionDialogComponent, CadeiraSelectionDialogData } from './cadeira-selection-dialog/cadeira-selection-dialog.component';

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
    MatDialogModule
  ],
  templateUrl: './professor-detail.component.html',
  styleUrls: ['./professor-detail.component.scss']
})
export class ProfessorDetailComponent implements OnInit, OnDestroy {
  professor: ProfessorDetailDto | null = null;
  isLoading = true;
  errorMessage: string | null = null;
  expandedCriterios: Set<number> = new Set(); // Para controlar quais critérios estão expandidos
  private routeSub: Subscription | undefined;
  private professorSub: Subscription | undefined;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private professorService: ProfessorService,
    private comentarioService: ComentarioService,
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

    if (!this.professor.cadeiras || this.professor.cadeiras.length === 0) {
      this.snackBar.open('Este professor não possui disciplinas cadastradas.', 'Fechar', { duration: 3000 });
      return;
    }

    // Se só há uma cadeira, navegar diretamente
    if (this.professor.cadeiras.length === 1) {
      this.router.navigate(['/avaliar/professor', this.professor.id, 'cadeira', this.professor.cadeiras[0].id]);
      return;
    }

    // Se há múltiplas cadeiras, abrir modal de seleção
    this.openCadeiraSelectionModal();
  }

  openCadeiraSelectionModal(): void {
    if (!this.professor || !this.professor.cadeiras) return;

    const dialogData: CadeiraSelectionDialogData = {
      cadeiras: this.professor.cadeiras,
      professorNome: this.professor.nomeCompleto
    };

    const dialogRef = this.dialog.open(CadeiraSelectionDialogComponent, {
      width: '600px',
      maxWidth: '90vw',
      data: dialogData,
      disableClose: false
    });

    dialogRef.afterClosed().subscribe((selectedCadeira: CadeiraSimplificada | undefined) => {
      if (selectedCadeira) {
        // Navegar para avaliação com a cadeira selecionada
        this.router.navigate(['/avaliar/professor', this.professor!.id, 'cadeira', selectedCadeira.id]);
      }
    });
  }

  verComentarios(cadeiraId: number): void {
    if (!this.professor) return;
    
    // Navegar para página de comentários específicos da cadeira
    this.router.navigate(['/professor', this.professor.id, 'cadeira', cadeiraId, 'comentarios']);
  }

  verEstatisticasCadeira(cadeiraId: number): void {
    if (!this.professor) return;
    
    // Navegar para a página da cadeira
    this.router.navigate(['/cadeira', cadeiraId]);
  }

  avaliarProfessor(cadeiraId: number): void {
    if (!this.professor) return;
    
    // Navegar diretamente para avaliação da cadeira específica
    this.router.navigate(['/avaliar/professor', this.professor.id, 'cadeira', cadeiraId]);
  }

  // Helper methods para o template
  getStarsArray(rating: number): boolean[] {
    const stars = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(i <= rating);
    }
    return stars;
  }

  getStars(rating: number): boolean[] {
    const filled = [];
    for (let i = 1; i <= Math.floor(rating); i++) {
      filled.push(true);
    }
    return filled;
  }

  getEmptyStars(rating: number): boolean[] {
    const empty = [];
    for (let i = Math.floor(rating) + 1; i <= 5; i++) {
      empty.push(false);
    }
    return empty;
  }

  trackByCadeiraId(index: number, item: any): number {
    return item.cadeiraId;
  }

  trackByCriterioId(index: number, item: any): number {
    return item.criterio.id;
  }

  // Methods for voting (placeholder)
  voteComment(commentId: number, voteType: 'positive' | 'negative'): void {
    const voteMethod = voteType === 'positive' ? 
      this.comentarioService.upvoteComentario(commentId) : 
      this.comentarioService.downvoteComentario(commentId);

    voteMethod.subscribe({
      next: (updatedComment) => {
        this.snackBar.open(`Voto ${voteType === 'positive' ? 'positivo' : 'negativo'} registrado!`, 'Fechar', { duration: 2000 });
        // Atualizar o comentário no professor se necessário
        this.updateCommentInProfessor(updatedComment);
      },
      error: (error) => {
        this.snackBar.open('Erro ao registrar voto.', 'Fechar', { duration: 3000 });
        console.error('Error voting on comment:', error);
      }
    });
  }

  verMaisComentarios(criterioId: number): void {
    if (!this.professor) return;
    
    // Navegar para a página de comentários por critério
    this.router.navigate(['/professor', this.professor.id, 'criterio', criterioId, 'comentarios']);
  }

  isCriterioExpanded(criterioId: number): boolean {
    return this.expandedCriterios.has(criterioId);
  }

  private updateCommentInProfessor(updatedComment: any): void {
    if (!this.professor || !this.professor.criteriosComMedias) return;
    
    // Encontrar e atualizar o comentário na estrutura do professor
    this.professor.criteriosComMedias.forEach(criterioMedia => {
      if (criterioMedia.topComentario && criterioMedia.topComentario.id === updatedComment.id) {
        criterioMedia.topComentario.score = updatedComment.score;
      }
    });
  }

  ngOnDestroy(): void {
    if (this.routeSub) {
      this.routeSub.unsubscribe();
    }
    if (this.professorSub) {
      this.professorSub.unsubscribe();
    }
  }
}
