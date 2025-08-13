import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProfessorService } from '../../services/professor.service';
import { ProfessorDetailDto, CadeiraNota } from '../../models/professor.model';
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
import { RouterLink } from '@angular/router';

// Mock interfaces for missing data
interface Comment {
  id: number;
  texto: string;
  autor: string;
  dataComentario: string;
  votosPositivos: number;
  votosNegativos: number;
  userVote: 'positive' | 'negative' | null;
}

interface CriterioWithComments {
  criterio: any;
  mediaNotas: number;
  topComentario?: Comment;
  comentarios: Comment[];
}

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

  /**
   * Helper method to reload professor data
   */
  loadProfessorData(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.fetchProfessorDetails(+id);
    }
  }

  /**
   * Helper method to generate filled stars array for rating display
   */
  getStars(rating: number): any[] {
    const fullStars = Math.floor(rating);
    return new Array(fullStars);
  }

  /**
   * Helper method to generate empty stars array for rating display
   */
  getEmptyStars(rating: number): any[] {
    const fullStars = Math.floor(rating);
    const emptyStars = 5 - fullStars;
    return new Array(emptyStars);
  }

  /**
   * Initialize mock data for demonstration purposes
   */
  private initializeMockData(): void {
    // Mock general comments
    this.mockGeneralComments = [
      {
        id: 1,
        texto: "Excelente professor! Suas aulas são muito didáticas e ele sempre está disponível para esclarecer dúvidas.",
        autor: "João Silva",
        dataComentario: "2024-08-01",
        votosPositivos: 15,
        votosNegativos: 2,
        userVote: null
      },
      {
        id: 2,
        texto: "Professor muito competente, mas às vezes suas explicações são um pouco rápidas demais.",
        autor: "Maria Santos",
        dataComentario: "2024-07-28",
        votosPositivos: 8,
        votosNegativos: 3,
        userVote: null
      },
      {
        id: 3,
        texto: "Recomendo muito! Professor que realmente se importa com o aprendizado dos alunos.",
        autor: "Carlos Oliveira",
        dataComentario: "2024-07-25",
        votosPositivos: 12,
        votosNegativos: 1,
        userVote: null
      }
    ];

    // Mock criteria with comments will be populated after professor data loads
  }

  /**
   * Create mock criteria with comments based on actual professor data
   */
  private createMockCriteriosWithComments(): void {
    if (!this.professor || !this.professor.criteriosComMedias) return;

    this.mockCriteriosWithComments = this.professor.criteriosComMedias.map((criterioOriginal, index) => {
      const mockComments: Comment[] = [
        {
          id: index * 10 + 1,
          texto: `Este professor demonstra ${criterioOriginal.criterio.nome.toLowerCase()} excepcional em suas aulas.`,
          autor: "Estudante Anônimo",
          dataComentario: "2024-08-05",
          votosPositivos: Math.floor(Math.random() * 20) + 5,
          votosNegativos: Math.floor(Math.random() * 5),
          userVote: null
        },
        {
          id: index * 10 + 2,
          texto: `Concordo parcialmente. O professor tem boas qualidades em ${criterioOriginal.criterio.nome.toLowerCase()}, mas pode melhorar.`,
          autor: "Maria Costa",
          dataComentario: "2024-08-03",
          votosPositivos: Math.floor(Math.random() * 15) + 3,
          votosNegativos: Math.floor(Math.random() * 7),
          userVote: null
        }
      ];

      return {
        criterio: criterioOriginal.criterio,
        mediaNotas: criterioOriginal.mediaNotas,
        topComentario: mockComments[0],
        comentarios: mockComments
      };
    });
  }

  /**
   * Create mock grades per subject based on actual professor data
   */
  private createMockCadeiraNotas(): void {
    if (!this.professor || !this.professor.cadeiras) return;

    this.mockCadeiraNotas = this.professor.cadeiras.map((cadeira, index) => {
      // Generate realistic mock data for each subject
      const baseRating = this.professor!.notaGeral || 4.0;
      const variation = (Math.random() - 0.5) * 1.0; // ±0.5 variation
      const notaMedia = Math.max(1.0, Math.min(5.0, baseRating + variation));
      const totalAvaliacoes = Math.floor(Math.random() * 50) + 10; // 10-59 evaluations

      return {
        cadeira: cadeira,
        notaMedia: Math.round(notaMedia * 10) / 10, // Round to 1 decimal
        totalAvaliacoes: totalAvaliacoes
      };
    });
  }

  /**
   * Vote on a comment (positive or negative)
   */
  voteOnComment(comment: Comment, voteType: 'positive' | 'negative'): void {
    // Remove previous vote if exists
    if (comment.userVote === 'positive') {
      comment.votosPositivos--;
    } else if (comment.userVote === 'negative') {
      comment.votosNegativos--;
    }

    // Apply new vote
    if (comment.userVote === voteType) {
      // User is removing their vote
      comment.userVote = null;
    } else {
      // User is adding/changing their vote
      if (voteType === 'positive') {
        comment.votosPositivos++;
      } else {
        comment.votosNegativos++;
      }
      comment.userVote = voteType;
    }

    // Show feedback
    const action = comment.userVote ? 'adicionado' : 'removido';
    const type = voteType === 'positive' ? 'positivo' : 'negativo';
    this.snackBar.open(`Voto ${type} ${action}!`, 'Fechar', { duration: 2000 });
  }

  /**
   * Get net votes (positive - negative) for a comment
   */
  getNetVotes(comment: Comment): number {
    return comment.votosPositivos - comment.votosNegativos;
  }

  /**
   * Track function for ngFor optimization
   */
  trackByCadeiraId(index: number, item: CadeiraNota): number {
    return item.cadeira.id;
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    this.professorSub?.unsubscribe();
  }
} 