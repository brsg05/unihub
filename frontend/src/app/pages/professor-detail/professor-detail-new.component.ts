import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProfessorService } from '../../services/professor.service';
import { ProfessorDetailDto } from '../../models/professor.model';
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
    private snackBar: MatSnackBar
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

    // Navegar para página de avaliação (implementar depois)
    this.router.navigate(['/avaliar/professor', this.professor.id]);
  }

  verComentarios(cadeiraId: number): void {
    if (!this.professor) return;
    
    // Navegar para página de comentários (implementar depois)
    this.router.navigate(['/professor', this.professor.id, 'cadeira', cadeiraId, 'comentarios']);
  }

  avaliarProfessor(cadeiraId: number): void {
    if (!this.professor) return;
    
    // Navegar para página de avaliação específica da cadeira
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

  trackByCadeiraId(index: number, item: any): number {
    return item.cadeiraId;
  }

  trackByCriterioId(index: number, item: any): number {
    return item.criterio.id;
  }

  // Methods for voting (placeholder)
  voteComment(commentId: number, voteType: 'positive' | 'negative'): void {
    // Implementar sistema de votação
    console.log(`Vote ${voteType} for comment ${commentId}`);
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
