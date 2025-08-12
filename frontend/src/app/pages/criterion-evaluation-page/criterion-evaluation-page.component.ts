import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Observable, Subscription } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { AvaliacaoService } from '../../core/services/avaliacao.service';
import { AvaliacaoPublic } from '../../models/avaliacao-public.model';
import { Page } from '../../models/professor.model'; // Assuming generic Page model
import { MatSnackBar } from '@angular/material/snack-bar';
// For filters, you might need MatSelectModule, FormsModule, etc.
import { CommonModule } from '@angular/common';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { CriterioService } from '../../core/services/criterio.service';
import { Criterio } from '../../models/criterio.model';
import { ProfessorService } from '../../core/services/professor.service';
import { Professor } from '../../models/professor.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-criterion-evaluation-page',
  standalone: true,
  imports: [
    CommonModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatListModule,
    MatFormFieldModule,
    MatSelectModule,
    FormsModule,
    MatPaginatorModule,
    MatIconModule,
    MatChipsModule,
    RouterLink
  ],
  templateUrl: './criterion-evaluation-page.component.html',
  styleUrls: ['./criterion-evaluation-page.component.scss']
})
export class CriterionEvaluationPageComponent implements OnInit, OnDestroy {
  professorId!: number;
  criterioId!: number;
  professor: Professor | null = null;
  criterio: Criterio | null = null;

  evaluations: AvaliacaoPublic[] = [];
  isLoading = true;
  totalElements = 0;
  pageSize = 10;
  currentPage = 0;
  currentPeriodo: string | null = null;
  currentSort: string | null = null; // 'top', 'worst', 'recent' (if backend supports sorting by score/date)

  periodos: string[] = []; // Populate with available periodos from evaluations or a dedicated endpoint

  private routeSub: Subscription | undefined;
  private evaluationsSub: Subscription | undefined;
  private professorSub: Subscription | undefined;
  private criterioSub: Subscription | undefined;

  constructor(
    private route: ActivatedRoute,
    private avaliacaoService: AvaliacaoService,
    private professorService: ProfessorService,
    private criterioService: CriterioService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.routeSub = this.route.paramMap.pipe(
      switchMap(params => {
        this.professorId = +params.get('professorId')!;
        this.criterioId = +params.get('criterioId')!;
        this.loadProfessorDetails();
        this.loadCriterioDetails();
        return this.loadEvaluations(); // Initial load
      })
    ).subscribe();
  }

  loadProfessorDetails(): void {
    this.professorSub = this.professorService.getProfessorById(this.professorId).subscribe(prof => this.professor = prof);
  }

  loadCriterioDetails(): void {
    this.criterioSub = this.criterioService.getCriterioById(this.criterioId).subscribe(crit => this.criterio = crit);
  }

  loadEvaluations(): Observable<Page<AvaliacaoPublic>> {
    this.isLoading = true;
    // The backend endpoint for criterion history seems to support a 'periodo' filter.
    // For 'top', 'worst', 'recent' sorting, the backend would need to support it directly
    // or we'd have to fetch all and sort client-side (less ideal for many comments).
    // For now, let's assume backend supports a general fetch and we handle some filtering/sorting aspects as possible.
    // The current backend getCriterionEvaluationHistory does not have explicit sort for top/worst rated comments.
    // We will use the period filter for now.
    const observable = this.avaliacaoService.getCriterionEvaluationHistory(
      this.professorId,
      this.criterioId,
      this.currentPeriodo,
      this.currentPage,
      this.pageSize
    );

    this.evaluationsSub = observable.subscribe({
      next: (data) => {
        this.evaluations = data.content;
        this.totalElements = data.totalElements;
        // Populate periodos for filter dropdown if not already done
        if (this.periodos.length === 0 && data.content.length > 0) {
          // This is a simple way; ideally, backend provides distinct periodos
          const uniquePeriodos = [...new Set(data.content.map(ev => ev.periodo))].sort().reverse();
          this.periodos = uniquePeriodos;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.snackBar.open('Erro ao carregar avaliações do critério.', 'Fechar', { duration: 3000 });
        this.isLoading = false;
        console.error(err);
      }
    });
    return observable; // switchMap needs an observable
  }

  pageChanged(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadEvaluations().subscribe();
  }

  onPeriodoChange(periodo: string | null): void {
    this.currentPeriodo = periodo;
    this.currentPage = 0; // Reset to first page
    this.loadEvaluations().subscribe();
  }

  onSortChange(sortType: string | null): void {
    this.currentSort = sortType;
    this.currentPage = 0;
    // TODO: Implement sorting. If backend doesn't sort comments by score directly,
    // this would require client-side sorting or a different backend endpoint/parameter.
    // For now, this is a placeholder.
    this.snackBar.open(`Filtro '${sortType}' a ser implementado.`, 'Fechar', {duration: 2000});
    this.loadEvaluations().subscribe(); // Reload, though backend might not use this sort parameter yet
  }

  /**
   * Reset all filters to default values
   */
  resetFilters(): void {
    this.currentPeriodo = null;
    this.currentSort = null;
    this.currentPage = 0;
    this.loadEvaluations().subscribe();
  }

  /**
   * Calculate average rating from all evaluations
   */
  getAverageRating(): number {
    if (!this.evaluations.length) return 0;
    
    // Since evaluations might have multiple criteria ratings, we need to find the specific criterion
    const ratings = this.evaluations
      .map(evaluation => {
        // Find the specific criterion rating in notas array
        const criterionRating = evaluation.notas?.find(nota => nota.criterioNome === this.criterio?.nome);
        return criterionRating?.nota || 0;
      })
      .filter(rating => rating > 0);
    
    return ratings.length > 0 ? ratings.reduce((sum, rating) => sum + rating, 0) / ratings.length : 0;
  }

  /**
   * Get stars array for rating display
   */
  getStarsArray(rating: number): number[] {
    const filledStars = Math.floor(rating || 0);
    return Array(filledStars).fill(0).map((_, i) => i + 1);
  }

  /**
   * Get empty stars array for rating display
   */
  getEmptyStarsArray(rating: number): number[] {
    const filledStars = Math.floor(rating || 0);
    const emptyStars = 5 - filledStars;
    return Array(emptyStars).fill(0).map((_, i) => filledStars + i + 1);
  }

  /**
   * TrackBy function for evaluations
   */
  trackByEvaluationId(index: number, evaluation: AvaliacaoPublic): number {
    return evaluation.id;
  }

  /**
   * TrackBy function for comments
   */
  trackByCommentId(index: number, comment: any): number {
    return comment.id;
  }

  /**
   * Vote on a comment (mock implementation)
   */
  voteComment(comment: any, voteType: 'up' | 'down'): void {
    // Mock implementation - in real app this would call a service
    if (!comment.upvotes) comment.upvotes = 0;
    if (!comment.downvotes) comment.downvotes = 0;
    if (!comment.userVote) comment.userVote = null;
    
    // Remove previous vote
    if (comment.userVote === 'up') comment.upvotes--;
    if (comment.userVote === 'down') comment.downvotes--;
    
    // Apply new vote or remove if same
    if (comment.userVote === voteType) {
      comment.userVote = null;
    } else {
      comment.userVote = voteType;
      if (voteType === 'up') comment.upvotes++;
      else comment.downvotes++;
    }
    
    const action = comment.userVote ? 'adicionado' : 'removido';
    this.snackBar.open(`Voto ${action}!`, 'Fechar', { duration: 2000 });
  }

  /**
   * Report a comment (mock implementation)
   */
  reportComment(comment: any): void {
    // Mock implementation - in real app this would open a dialog or call a service
    this.snackBar.open('Comentário reportado! Nossa equipe irá revisar.', 'Fechar', { duration: 3000 });
  }

  /**
   * Get the rating for a specific criterion in an evaluation
   */
  getCriterionRating(evaluation: AvaliacaoPublic): number {
    const criterionName = this.criterio?.nome;
    if (!criterionName) return 0;
    
    const criterionRating = evaluation.notas?.find(nota => nota.criterioNome === criterionName);
    return criterionRating?.nota || 0;
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    this.evaluationsSub?.unsubscribe();
    this.professorSub?.unsubscribe();
    this.criterioSub?.unsubscribe();
  }
} 