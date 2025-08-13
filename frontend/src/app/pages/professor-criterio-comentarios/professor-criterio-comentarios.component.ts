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
import { ProfessorDetailDto } from '../../models/professor.model';
import { Cadeira } from '../../models/cadeira.model';
import { HttpClient, HttpParams } from '@angular/common/http';
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
  selector: 'app-professor-criterio-comentarios',
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
          <h1>Comentários - {{ criterioNome }}</h1>
          <p *ngIf="professorNome">
            <strong>Professor:</strong> {{ professorNome }}
          </p>
        </div>
      </div>

      <!-- Filtros -->
      <div class="filters-section" *ngIf="!loading">
        <div class="filters-grid">
          <!-- Filtro de Cadeira -->
          <mat-form-field appearance="outline">
            <mat-label>Cadeira</mat-label>
            <mat-select [(value)]="selectedCadeiraId" (selectionChange)="onFilterChange()">
              <mat-option value="">Todas as cadeiras</mat-option>
              <mat-option *ngFor="let cadeira of cadeiras" [value]="cadeira.id">
                {{ cadeira.nome }}
              </mat-option>
            </mat-select>
          </mat-form-field>

          <!-- Filtro de Período -->
          <mat-form-field appearance="outline">
            <mat-label>Período</mat-label>
            <mat-select [(value)]="selectedPeriodo" (selectionChange)="onFilterChange()">
              <mat-option value="">Todos os períodos</mat-option>
              <mat-option value="ultima-semana">Última semana</mat-option>
              <mat-option value="ultimo-mes">Último mês</mat-option>
              <mat-option value="ultimo-semestre">Último semestre</mat-option>
              <mat-option value="ultimo-ano">Último ano</mat-option>
            </mat-select>
          </mat-form-field>

          <!-- Filtro de Ordenação -->
          <mat-form-field appearance="outline">
            <mat-label>Ordenar por</mat-label>
            <mat-select [(value)]="selectedOrdenacao" (selectionChange)="onFilterChange()">
              <mat-option value="mais-recentes">Mais recentes</mat-option>
              <mat-option value="mais-antigos">Mais antigos</mat-option>
              <mat-option value="mais-positivos">Mais positivos</mat-option>
              <mat-option value="mais-negativos">Mais negativos</mat-option>
            </mat-select>
          </mat-form-field>
        </div>
      </div>

      <!-- Loading -->
      <div *ngIf="loading" class="loading-container">
        <mat-spinner></mat-spinner>
        <p>Carregando comentários...</p>
      </div>

      <!-- Lista de Comentários -->
      <div *ngIf="!loading && comentariosFiltrados.length > 0" class="comentarios-list">
        <mat-card *ngFor="let comentario of comentariosFiltrados; trackBy: trackByComentarioId" class="comentario-card">
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
            <div class="cadeira-info" *ngIf="comentario.cadeiraNome">
              <mat-chip-set>
                <mat-chip>{{ comentario.cadeiraNome }}</mat-chip>
              </mat-chip-set>
            </div>

            <div class="comentario-texto">
              <p>{{ comentario.texto }}</p>
            </div>

            <div class="voting-section" *ngIf="comentario.id">
              <!-- Debug info -->
              <div style="font-size: 10px; color: #999; margin-bottom: 8px;">
                Debug: Pos={{ comentario.votosPositivos }} | Neg={{ comentario.votosNegativos }} | Score={{ comentario.score }}
              </div>
              
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
              <div class="score-display">
                <span class="score">Score: {{ comentario.score }}</span>
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
        [pageIndex]="pageIndex"
        [pageSizeOptions]="[5, 10, 25, 100]"
        (page)="onPageChange($event)"
        aria-label="Select page">
      </mat-paginator>

      <!-- Estado vazio -->
      <div *ngIf="!loading && comentariosFiltrados.length === 0" class="empty-state">
        <mat-icon class="empty-icon">comment_off</mat-icon>
        <h3>Nenhum comentário encontrado</h3>
        <p *ngIf="comentarios.length === 0">Este critério ainda não possui comentários de avaliações.</p>
        <p *ngIf="comentarios.length > 0">Nenhum comentário corresponde aos filtros selecionados.</p>
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

    .filters-section {
      margin-bottom: 30px;
      padding: 20px;
      background: #f8f9fa;
      border-radius: 8px;
    }

    .filters-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
    }

    .filters-grid mat-form-field {
      width: 100%;
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

    .comentario-texto {
      margin-bottom: 16px;
    }

    .cadeira-info {
      margin-bottom: 12px;
    }

    .cadeira-info mat-chip {
      background-color: #e3f2fd;
      color: #1976d2;
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
      display: flex;
      justify-content: space-between;
      align-items: center;
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

    .score-display {
      color: #666;
      font-weight: 500;
    }

    .score {
      font-size: 14px;
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

      .voting-section {
        flex-direction: column;
        align-items: flex-start;
        gap: 12px;
      }

      .vote-buttons {
        justify-content: center;
        width: 100%;
      }
    }
  `]
})
export class ProfessorCriterioComentariosComponent implements OnInit {
  professorId!: number;
  criterioId!: number;
  professorNome: string = '';
  criterioNome: string = '';
  comentarios: ComentarioDto[] = [];
  comentariosFiltrados: ComentarioDto[] = [];
  cadeiras: Cadeira[] = [];
  loading = true;
  
  // Filtros
  selectedCadeiraId: string = '';
  selectedPeriodo: string = '';
  selectedOrdenacao: string = 'mais-recentes';
  
  // Paginação
  totalComentarios = 0;
  pageIndex = 0;
  pageSize = 10;
  
  private apiUrl = environment.apiUrl;

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
      this.criterioId = +params['criterioId'];
      
      console.log('Parâmetros da rota:', { professorId: this.professorId, criterioId: this.criterioId });
      
      this.loadData();
    });
  }

  onFilterChange(): void {
    this.loadData(true);
  }

  private loadData(resetPage: boolean = false): void {
    if (resetPage) {
      this.pageIndex = 0;
    }
    
    this.loading = true;
    
    // Carregar informações do professor
    this.professorService.getProfessorById(this.professorId).subscribe({
      next: (professor: ProfessorDetailDto) => {
        this.professorNome = professor.nomeCompleto;
        
        // Encontrar o nome do critério
        const criterio = professor.criteriosComMedias?.find(c => c.criterio.id === this.criterioId);
        this.criterioNome = criterio ? criterio.criterio.nome : 'Critério';
        
        // Carregar cadeiras do professor
        this.loadCadeiras();
      },
      error: (error: any) => {
        console.error('Erro ao carregar professor:', error);
      }
    });

    // Carregar comentários do critério
    this.getComentariosPorCriterioEProfessor(this.professorId, this.criterioId).subscribe({
      next: (response: any) => {
        console.log('Comentários carregados:', response);
        this.comentarios = response.content;
        this.totalComentarios = response.totalElements;
        this.comentariosFiltrados = this.comentarios; // Apenas atribui, sem filtro client-side
        this.loading = false;
      },
      error: (error: any) => {
        console.error('Erro ao carregar comentários:', error);
        this.comentarios = [];
        this.comentariosFiltrados = [];
        this.loading = false;
      }
    });
  }

  private loadCadeiras(): void {
    // Carregar cadeiras através do professor
    this.professorService.getProfessorById(this.professorId).subscribe({
      next: (professor: ProfessorDetailDto) => {
        // Usar as cadeiras diretamente do professor
        if (professor.cadeiras) {
          this.cadeiras = professor.cadeiras.map(cadeiraSimp => ({
            id: cadeiraSimp.id,
            nome: cadeiraSimp.nome,
            codigo: '', // Não disponível na versão simplificada
            descricao: '', // Não disponível na versão simplificada
            cargaHoraria: 0, // Não disponível na versão simplificada
            isEletiva: false, // Não disponível na versão simplificada
            cursoId: 0 // Não disponível na versão simplificada
          }));
        } else {
          this.cadeiras = [];
        }
        
        console.log('Cadeiras do professor carregadas:', this.cadeiras);
      },
      error: (error: any) => {
        console.error('Erro ao carregar cadeiras do professor:', error);
        this.cadeiras = [];
      }
    });
  }

  private getComentariosPorCriterioEProfessor(professorId: number, criterioId: number): Observable<any> {
    let params = new HttpParams()
      .set('page', this.pageIndex.toString())
      .set('size', this.pageSize.toString())
      .set('sort', this.getSortParameter());

    if (this.selectedCadeiraId) {
      params = params.set('cadeiraId', this.selectedCadeiraId.toString());
    }
    
    if (this.selectedPeriodo) {
      params = params.set('periodo', this.selectedPeriodo);
    }

    const url = `${this.apiUrl}/comentarios/professor/${professorId}/criterio/${criterioId}`;
    console.log('Fazendo requisição para:', url, 'com parâmetros:', params.toString());
    return this.http.get<any>(url, { params });
  }

  private getSortParameter(): string {
    switch (this.selectedOrdenacao) {
      case 'mais-recentes':
        return 'createdAt,desc';
      case 'mais-antigos':
        return 'createdAt,asc';
      case 'mais-positivos':
        return 'votosPositivos,desc';
      case 'mais-negativos':
        return 'votosNegativos,desc';
      default:
        return 'createdAt,desc';
    }
  }

  upvoteComment(comentarioId: number): void {
    this.comentarioService.upvoteComentario(comentarioId).subscribe({
      next: () => {
        // Recarregar os dados atuais
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
        // Recarregar os dados atuais
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
