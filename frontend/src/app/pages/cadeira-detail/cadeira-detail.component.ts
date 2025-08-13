import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { Cadeira } from '../../models/cadeira.model';
import { ProfessorDto } from '../../models/professor.model';

interface ProfessorCadeira {
  id: number;
  nomeCompleto: string;
  notaGeral: number;
  totalAvaliacoes: number;
  periodo?: string;
  foto?: string;
}

interface CadeiraDetailed extends Cadeira {
  codigo?: string;
  descricao?: string;
  notaGeral?: number;
  professorCount?: number;
  professores: ProfessorCadeira[];
  totalAvaliacoes: number;
  avaliacoesPorCriterio: {
    didatica: number;
    clareza: number;
    pontualidade: number;
    disponibilidade: number;
    organizacao: number;
  };
}

@Component({
  selector: 'app-cadeira-detail',
  templateUrl: './cadeira-detail.component.html',
  styleUrls: ['./cadeira-detail.component.scss']
})
export class CadeiraDetailComponent implements OnInit, OnDestroy {
  cadeira: CadeiraDetailed | null = null;
  cadeiraId: number | null = null;
  isLoading = true;
  errorMessage: string | null = null;

  private destroy$ = new Subject<void>();

  // Mock data
  mockCadeiraDetail: CadeiraDetailed = {
    id: 1,
    nome: 'Algoritmos e Estruturas de Dados',
    codigo: 'AED001',
    cargaHoraria: 60,
    isEletiva: false,
    cursoId: 1,
    cursoNome: 'Ciência da Computação',
    descricao: 'Esta disciplina aborda os conceitos fundamentais de algoritmos e estruturas de dados, incluindo análise de complexidade, estruturas lineares e não-lineares, algoritmos de ordenação e busca, e técnicas de programação avançadas.',
    professorCount: 3,
    notaGeral: 4.2,
    totalAvaliacoes: 127,
    professores: [
      {
        id: 1,
        nomeCompleto: 'Dr. João Silva',
        notaGeral: 4.5,
        totalAvaliacoes: 45,
        periodo: '2024.1',
        foto: 'https://via.placeholder.com/150x150/4c6ef5/ffffff?text=JS'
      },
      {
        id: 2,
        nomeCompleto: 'Profa. Maria Santos',
        notaGeral: 4.1,
        totalAvaliacoes: 38,
        periodo: '2023.2',
        foto: 'https://via.placeholder.com/150x150/7c3aed/ffffff?text=MS'
      },
      {
        id: 3,
        nomeCompleto: 'Dr. Carlos Oliveira',
        notaGeral: 3.9,
        totalAvaliacoes: 44,
        periodo: '2024.1',
        foto: 'https://via.placeholder.com/150x150/059669/ffffff?text=CO'
      }
    ],
    avaliacoesPorCriterio: {
      didatica: 4.3,
      clareza: 4.1,
      pontualidade: 4.4,
      disponibilidade: 4.0,
      organizacao: 4.2
    }
  };

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.paramMap
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        const id = params.get('id');
        if (id) {
          this.cadeiraId = parseInt(id, 10);
          this.loadCadeiraDetail();
        } else {
          this.router.navigate(['/cadeiras']);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadCadeiraDetail(): void {
    this.isLoading = true;
    this.errorMessage = null;

    // Simulate API call
    setTimeout(() => {
      try {
        // In real implementation, call service with this.cadeiraId
        this.cadeira = this.mockCadeiraDetail;
        this.isLoading = false;
      } catch (error) {
        this.errorMessage = 'Erro ao carregar detalhes da cadeira. Tente novamente.';
        this.isLoading = false;
      }
    }, 1000);
  }

  getStarsArray(rating: number): number[] {
    return Array(Math.floor(rating)).fill(0);
  }

  getEmptyStarsArray(rating: number): number[] {
    return Array(5 - Math.floor(rating)).fill(0);
  }

  getProgressBarWidth(rating: number): string {
    return `${(rating / 5) * 100}%`;
  }

  viewProfessor(professorId: number): void {
    this.router.navigate(['/professor', professorId]);
  }

  avaliarProfessor(professorId: number): void {
    if (this.cadeira) {
      this.router.navigate(['/avaliar/professor', professorId, 'cadeira', this.cadeira.id]);
    }
  }

  goBack(): void {
    this.router.navigate(['/cadeiras']);
  }

  getCriterioDisplayName(criterio: string): string {
    const criterios: { [key: string]: string } = {
      'didatica': 'Didática',
      'clareza': 'Clareza',
      'pontualidade': 'Pontualidade',
      'disponibilidade': 'Disponibilidade',
      'organizacao': 'Organização'
    };
    return criterios[criterio] || criterio;
  }

  getCriterioIcon(criterio: string): string {
    const icons: { [key: string]: string } = {
      'didatica': 'bi-person-video2',
      'clareza': 'bi-lightbulb',
      'pontualidade': 'bi-clock',
      'disponibilidade': 'bi-chat-dots',
      'organizacao': 'bi-list-check'
    };
    return icons[criterio] || 'bi-star';
  }
}
