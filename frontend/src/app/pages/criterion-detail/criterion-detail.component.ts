import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription, forkJoin, map, switchMap } from 'rxjs';
import { ProfessorService } from '../../core/services/professor.service';
import { CriterioService } from '../../core/services/criterio.service';
import { AvaliacaoService } from '../../core/services/avaliacao.service';
import { Professor, Page } from '../../models/professor.model';
import { Criterio } from '../../models/criterio.model';
import { AvaliacaoPublic } from '../../models/avaliacao-public.model';
import { ComentarioPublic } from '../../models/comentario-public.model';
import { AvaliacaoNotaPublic } from '../../models/avaliacao-nota-public.model';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';

// Interface for the data we want to display for each evaluation instance
export interface MappedAvaliacaoParaCriterio {
  dataAvaliacao: string;
  periodoAvaliacao: string;
  nota: number;
  comentariosGeraisDaAvaliacao: ComentarioPublic[];
  nomeCadeira?: string;
}

@Component({
  selector: 'app-criterion-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatProgressSpinnerModule,
    MatListModule,
    MatDividerModule,
    MatTableModule,
    MatIconModule
  ],
  templateUrl: './criterion-detail.component.html',
  styleUrls: ['./criterion-detail.component.scss']
})
export class CriterionDetailComponent implements OnInit, OnDestroy {
  professor: Professor | null = null;
  criterio: Criterio | null = null;
  avaliacoesMostradas: MappedAvaliacaoParaCriterio[] = [];
  // Pagination properties for criterion evaluations
  currentPage = 0;
  pageSize = 10;
  totalElements = 0;
  isLoading = true;
  isLoadingMore = false;
  errorMessage: string | null = null;
  
  private routeSub: Subscription | undefined;
  private dataSub: Subscription | undefined;
  private currentProfessorId!: number;
  private currentCriterioId!: number;

  constructor(
    private route: ActivatedRoute,
    private professorService: ProfessorService,
    private criterioService: CriterioService,
    private avaliacaoService: AvaliacaoService,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    this.routeSub = this.route.paramMap.pipe(
      switchMap(params => {
        const profId = params.get('profId');
        const critId = params.get('critId');

        if (profId && critId) {
          this.currentProfessorId = +profId;
          this.currentCriterioId = +critId;
          this.isLoading = true;
          this.currentPage = 0;
          this.avaliacoesMostradas = [];
          // First, fetch professor and criterion details
          return forkJoin({
            professor: this.professorService.getProfessorById(this.currentProfessorId),
            criterio: this.criterioService.getCriterioById(this.currentCriterioId)
          });
        } else {
          this.isLoading = false;
          this.errorMessage = 'IDs do professor e/ou critério não encontrados na URL.';
          this.snackBar.open(this.errorMessage, 'Fechar', { duration: 5000 });
          throw new Error(this.errorMessage);
        }
      })
    ).subscribe({
      next: ({ professor, criterio }) => {
        this.professor = professor;
        this.criterio = criterio;
        if (this.criterio) {
          this.loadEvaluationsForCriterion();
        } else {
          this.isLoading = false;
          this.errorMessage = 'Detalhes do critério não puderam ser carregados.';
          this.snackBar.open(this.errorMessage, 'Fechar', { duration: 5000 });
        }
      },
      error: (err: HttpErrorResponse) => {
        this.handleError(err, 'Erro ao buscar detalhes do professor ou critério.');
      }
    });
  }

  loadEvaluationsForCriterion(loadMore = false): void {
    if (!this.currentProfessorId || !this.criterio || !this.criterio.nome) return;

    if (loadMore) {
      this.isLoadingMore = true;
    } else {
      this.isLoading = true;
    }
    this.errorMessage = null;

    this.dataSub = this.avaliacaoService.getCriterionEvaluationHistory(
      this.currentProfessorId,
      this.currentCriterioId,
      null,
      this.currentPage,
      this.pageSize
    ).subscribe({
      next: (pageData: Page<AvaliacaoPublic>) => {
        const criterioNomeAtual = this.criterio!.nome;

        const mappedData = pageData.content.map((aval: AvaliacaoPublic) => {
          const notaCriterioEspecifico = aval.notas?.find((notaDto: AvaliacaoNotaPublic) => notaDto.criterioNome === criterioNomeAtual);
          
          return {
            dataAvaliacao: aval.data,
            periodoAvaliacao: aval.periodo,
            nomeCadeira: aval.cadeiraNome,
            nota: notaCriterioEspecifico ? notaCriterioEspecifico.nota : 0,
            comentariosGeraisDaAvaliacao: aval.comentarios || []
          };
        }).filter(item => item !== null) as MappedAvaliacaoParaCriterio[];
        
        this.avaliacoesMostradas = loadMore ? [...this.avaliacoesMostradas, ...mappedData] : mappedData;
        this.totalElements = pageData.totalElements;
        this.isLoading = false;
        this.isLoadingMore = false;
      },
      error: (err: HttpErrorResponse) => {
        this.handleError(err, 'Erro ao buscar histórico de avaliações do critério.');
        this.isLoadingMore = false;
      }
    });
  }

  onScroll(): void {
    if (!this.isLoading && !this.isLoadingMore && this.avaliacoesMostradas.length < this.totalElements) {
      this.currentPage++;
      this.loadEvaluationsForCriterion(true);
    }
  }

  private handleError(error: HttpErrorResponse, defaultMessage: string): void {
    this.isLoading = false;
    this.isLoadingMore = false;
    this.errorMessage = defaultMessage;
    if (error.status === 404) {
      this.errorMessage = `Recurso não encontrado. ${defaultMessage}`;
    }
    this.snackBar.open(this.errorMessage, 'Fechar', { duration: 5000 });
    console.error('Error in CriterionDetailComponent:', error);
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    this.dataSub?.unsubscribe();
  }
} 