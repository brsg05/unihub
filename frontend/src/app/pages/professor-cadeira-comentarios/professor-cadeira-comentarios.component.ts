import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormsModule } from '@angular/forms';
import { ComentarioService } from '../../core/services/comentario.service';
import { ProfessorService } from '../../core/services/professor.service';
import { CadeiraService } from '../../core/services/cadeira.service';
import { ProfessorDetailDto } from '../../models/professor.model';
import { Cadeira } from '../../models/cadeira.model';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';

// Interface matching backend ComentarioDto
interface ComentarioDto {
  id: number;
  texto: string;
  avaliacaoId: number;
  criterioId: number;
  criterioNome: string;
  cadeiraNome?: string;
  votosPositivos: number;
  votosNegativos: number;
  score: number;
  createdAt: string;
  userVoteType?: string | null; // "UPVOTE", "DOWNVOTE" ou null
}

@Component({
  selector: 'app-professor-cadeira-comentarios',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatSelectModule,
    MatFormFieldModule,
    MatInputModule,
    FormsModule,
    MatPaginatorModule
  ],
  template: `
    <div class="comentarios-container">
      <!-- Header -->
      <div class="header">
        <button mat-icon-button (click)="voltar()" class="back-button">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <div class="header-info">
          <h1>Comentários da Disciplina</h1>
          <p *ngIf="professorNome && cadeiraNome">
            <strong>Professor:</strong> {{ professorNome }} |
            <strong>Disciplina:</strong> {{ cadeiraNome }}
          </p>
        </div>
      </div>

      <!-- Loading -->
      <div *ngIf="loading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Carregando comentários...</p>
      </div>

      <!-- Lista de Comentários -->
      <div *ngIf="!loading && comentarios.length > 0" class="comentarios-list">
        <mat-card *ngFor="let comentario of comentarios; trackBy: trackByComentarioId" class="comentario-card">
          <mat-card-header>
            <div class="comentario-header">
              <div class="autor-info">
                <mat-icon class="user-icon">person</mat-icon>
                <span class="autor-nome">Usuário</span>
              </div>
              <div class="data-info">
                <mat-icon class="date-icon">schedule</mat-icon>
                <span class="data">{{ formatarData(comentario.createdAt) }}</span>
              </div>
            </div>
          </mat-card-header>

          <mat-card-content>
            <div class="criterio-info">
              <mat-chip-set>
                <mat-chip>{{ comentario.criterioNome }}</mat-chip>
              </mat-chip-set>
            </div>
            
            <div class="comentario-texto">
              <p>{{ comentario.texto }}</p>
            </div>

            <div class="voting-section" *ngIf="comentario.id">
              <div class="vote-buttons">
                <button 
                  mat-icon-button 
                  (click)="upvoteComment(comentario.id!)"
                  class="upvote-btn">
                  <mat-icon>thumb_up</mat-icon>
                  <span class="vote-count">{{ comentario.votosPositivos || 0 }}</span>
                </button>
                
                <button 
                  mat-icon-button 
                  (click)="downvoteComment(comentario.id!)"
                  class="downvote-btn">
                  <mat-icon>thumb_down</mat-icon>
                  <span class="vote-count">{{ comentario.votosNegativos || 0 }}</span>
                </button>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Paginação -->
      <mat-paginator
        *ngIf="!loading && totalComentarios > 0"
        [length]="totalComentarios"
        [pageSize]="pageSize"
        [pageSizeOptions]="[5, 10, 25, 100]"
        (page)="onPageChange($event)"
        aria-label="Select page">
      </mat-paginator>

      <!-- Estado vazio -->
      <div *ngIf="!loading && comentarios.length === 0" class="empty-state">
        <mat-icon class="empty-icon">comment_off</mat-icon>
        <h3>Nenhum comentário encontrado</h3>
        <p>Esta disciplina ainda não possui comentários de avaliações.</p>
        <button mat-raised-button color="primary" (click)="voltar()">
          Voltar
        </button>
      </div>
    </div>
  `,
  styles: [`
    .comentarios-container {
      max-width: 800px;
      margin: 0 auto;
      padding: 20px;
    }

    .header {
      display: flex;
      align-items: flex-start;
      margin-bottom: 30px;
      gap: 16px;
    }

    .back-button {
      margin-top: 4px;
    }

    .header-info h1 {
      margin: 0 0 8px 0;
      color: #333;
      font-size: 28px;
      font-weight: 600;
    }

    .header-info p {
      margin: 0;
      color: #666;
      font-size: 16px;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 60px 20px;
      color: #666;
    }

    .loading-container p {
      margin-top: 16px;
      font-size: 16px;
    }

    .comentarios-list {
      display: flex;
      flex-direction: column;
      gap: 20px;
    }

    .comentario-card {
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      border-radius: 12px;
      overflow: hidden;
    }

    .comentario-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: 100%;
    }

    .autor-info, .data-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #666;
    }

    .user-icon, .date-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    .autor-nome, .data {
      font-weight: 500;
    }

    .criterio-info {
      margin-bottom: 16px;
    }

    .criterio-info mat-chip {
      background-color: #e3f2fd;
      color: #1976d2;
      font-weight: 500;
    }

    .comentario-texto {
      margin-bottom: 16px;
    }

    .comentario-texto p {
      margin: 0;
      line-height: 1.6;
      color: #333;
      font-size: 16px;
    }

    .voting-section {
      border-top: 1px solid #eee;
      padding-top: 16px;
    }

    .vote-buttons {
      display: flex;
      gap: 16px;
    }

    .upvote-btn, .downvote-btn {
      display: flex;
      align-items: center;
      gap: 4px;
      border-radius: 20px;
      padding: 8px 12px;
      color: #666;
      transition: all 0.2s ease;
    }

    .upvote-btn:hover {
      background-color: #e8f5e8;
      color: #4caf50;
    }

    .downvote-btn:hover {
      background-color: #ffeaea;
      color: #f44336;
    }

    .vote-count {
      font-size: 14px;
      font-weight: 500;
      min-width: 20px;
      text-align: center;
    }

    .empty-state {
      text-align: center;
      padding: 60px 20px;
      color: #666;
    }

    .empty-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #ddd;
      margin-bottom: 16px;
    }

    .empty-state h3 {
      margin: 16px 0 8px 0;
      color: #333;
      font-size: 24px;
      font-weight: 500;
    }

    .empty-state p {
      margin: 0 0 24px 0;
      font-size: 16px;
    }

    @media (max-width: 600px) {
      .comentarios-container {
        padding: 16px;
      }

      .header {
        flex-direction: column;
        gap: 8px;
      }

      .header-info h1 {
        font-size: 24px;
      }

      .comentario-header {
        flex-direction: column;
        align-items: flex-start;
        gap: 8px;
      }

      .vote-buttons {
        justify-content: center;
      }
    }
  `]
})
export class ProfessorCadeiraComentariosComponent implements OnInit {
  professorId!: number;
  cadeiraId!: number;
  professorNome: string = '';
  cadeiraNome: string = '';
  comentarios: ComentarioDto[] = [];
  loading = true;
  private apiUrl = environment.apiUrl;

  // Paginação
  totalComentarios = 0;
  pageIndex = 0;
  pageSize = 10;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private comentarioService: ComentarioService,
    private professorService: ProfessorService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.professorId = +params['professorId'];
      this.cadeiraId = +params['cadeiraId'];
      
      this.loadData();
    });
  }

  private loadData(): void {
    this.loading = true;
    
    // Carregar informações do professor
    this.professorService.getProfessorById(this.professorId).subscribe({
      next: (professor: ProfessorDetailDto) => {
        this.professorNome = professor.nomeCompleto;
        
        // Encontrar o nome da cadeira
        const cadeira = professor.cadeiras?.find(c => c.id === this.cadeiraId) ||
                       professor.cadeiraNotas?.find(cn => cn.cadeiraId === this.cadeiraId);
        
        this.cadeiraNome = cadeira ? 
          (cadeira as any).nome || (cadeira as any).cadeiraNome : 
          'Disciplina';
      },
      error: (error: any) => {
        console.error('Erro ao carregar professor:', error);
      }
    });

    // Carregar comentários
    this.getComentariosPorProfessorECadeira(this.professorId, this.cadeiraId).subscribe({
      next: (response: any) => {
        this.comentarios = response.content;
        this.totalComentarios = response.totalElements;
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erro ao carregar comentários:', error);
        this.comentarios = [];
        this.loading = false;
      }
    });
  }

  private getComentariosPorProfessorECadeira(professorId: number, cadeiraId: number): Observable<any> {
    const url = `${this.apiUrl}/comentarios/professor/${professorId}/cadeira/${cadeiraId}?page=${this.pageIndex}&size=${this.pageSize}`;
    return this.http.get<any>(url);
  }

  upvoteComment(comentarioId: number): void {
    this.comentarioService.upvoteComentario(comentarioId).subscribe({
      next: () => {
        // Recarregar comentários para atualizar votos
        this.loadData();
      },
      error: (error: any) => {
        console.error('Erro ao votar no comentário:', error);
      }
    });
  }

  downvoteComment(comentarioId: number): void {
    this.comentarioService.downvoteComentario(comentarioId).subscribe({
      next: () => {
        // Recarregar comentários para atualizar votos
        this.loadData();
      },
      error: (error: any) => {
        console.error('Erro ao votar no comentário:', error);
      }
    });
  }

  formatarData(data: string): string {
    const date = new Date(data);
    return date.toLocaleDateString('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  voltar(): void {
    this.router.navigate(['/professor', this.professorId]);
  }

  trackByComentarioId(index: number, comentario: ComentarioDto): number {
    return comentario.id || index;
  }

  onPageChange(event: PageEvent) {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadData();
  }
}
