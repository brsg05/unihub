import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { MatSliderModule } from '@angular/material/slider'; // Para notas em slider

import { CriterioService } from '../../core/services/criterio.service';
import { ProfessorService } from '../../core/services/professor.service';
import { AvaliacaoService } from '../../core/services/avaliacao.service';
import { Criterio } from '../../models/criterio.model';
import { Professor } from '../../models/professor.model';
import { Cadeira } from '../../models/cadeira.model';
import { AvaliacaoRequest } from '../../models/avaliacao.model';

@Component({
  selector: 'app-avaliacao-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule,
    MatSliderModule
  ],
  templateUrl: './avaliacao-form.component.html',
  styleUrls: ['./avaliacao-form.component.scss']
})
export class AvaliacaoFormComponent implements OnInit, OnDestroy {
  avaliacaoForm!: FormGroup;
  professor: Professor | null = null;
  cadeira: Cadeira | null = null; // Ou apenas o nome da cadeira
  cadeiraNome: string | null = null; // Para simplificar se passarmos via state
  criterios: Criterio[] = [];
  
  isLoading = true;
  errorMessage: string | null = null;
  professorId!: number;
  cadeiraId!: number;

  private routeSub: Subscription | undefined;
  private criteriosSub: Subscription | undefined;
  private professorSub: Subscription | undefined;
  private avaliacaoSub: Subscription | undefined;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private fb: FormBuilder,
    private criterioService: CriterioService,
    private professorService: ProfessorService,
    private avaliacaoService: AvaliacaoService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.routeSub = this.route.paramMap.subscribe(params => {
      this.professorId = +params.get('professorId')!;
      this.cadeiraId = +params.get('cadeiraId')!;
      
      // Tentar pegar nome da cadeira do state da navegação (se passado)
      const navigation = this.router.getCurrentNavigation();
      this.cadeiraNome = navigation?.extras?.state?.['cadeiraNome'];

      if (this.professorId && this.cadeiraId) {
        this.loadInitialData();
      } else {
        this.errorMessage = 'IDs do professor ou cadeira inválidos.';
        this.isLoading = false;
      }
    });

    this.avaliacaoForm = this.fb.group({
      periodo: ['', Validators.required],
      avaliacoesCriterios: this.fb.array([])
    });
  }

  loadInitialData(): void {
    this.isLoading = true;
    this.professorSub = this.professorService.getProfessorById(this.professorId).subscribe({
      next: prof => {
        this.professor = prof;
        // Se não passamos cadeiraNome, e Cadeira não vem com ProfessorDto simples, teríamos que buscar Cadeira
        if (!this.cadeiraNome) {
          // Lógica para buscar detalhes da cadeira se necessário, ou assumir que o ID é suficiente para o backend.
          // Por simplicidade, vamos assumir que o backend só precisa do cadeiraId.
          // Se o objeto Cadeira completo fosse necessário, faríamos outra chamada aqui.
        }
        this.loadCriterios(); // Carregar critérios após professor
      },
      error: err => {
        this.errorMessage = 'Erro ao carregar dados do professor.';
        this.snackBar.open(this.errorMessage, 'Fechar', { duration: 5000 });
        this.isLoading = false;
      }
    });
  }

  loadCriterios(): void {
    this.criteriosSub = this.criterioService.getAllCriteriosList().subscribe({
      next: criterios => {
        this.criterios = criterios;
        this.buildFormForCriterios();
        this.isLoading = false;
      },
      error: err => {
        this.errorMessage = 'Erro ao carregar critérios de avaliação.';
        this.snackBar.open(this.errorMessage, 'Fechar', { duration: 5000 });
        this.isLoading = false;
      }
    });
  }

  get avaliacoesCriteriosArray(): FormArray {
    return this.avaliacaoForm.get('avaliacoesCriterios') as FormArray;
  }

  buildFormForCriterios(): void {
    this.criterios.forEach(criterio => {
      this.avaliacoesCriteriosArray.push(
        this.fb.group({
          criterioId: [criterio.id, Validators.required],
          criterioNome: [criterio.nome], // Apenas para exibição, não será enviado
          nota: [null, [Validators.required, Validators.min(1), Validators.max(5)]], // Ex: nota de 1 a 5
          comentario: [''] // Comentário opcional
        })
      );
    });
  }

  onSubmit(): void {
    if (this.avaliacaoForm.invalid) {
      this.snackBar.open('Formulário inválido. Verifique os campos, incluindo o período.', 'Fechar', { duration: 3000 });
      this.avaliacaoForm.markAllAsTouched();
      return;
    }

    const formValue = this.avaliacaoForm.value;
    
    // Construir o payload de acordo com AvaliacaoRequest
    // O modelo AvaliacaoRequest espera notasCriterios e comentarios separados.
    const notasParaEnviar = formValue.avaliacoesCriterios.map((ac: any) => ({
      criterioId: ac.criterioId,
      nota: ac.nota
    }));

    const comentariosParaEnviar = formValue.avaliacoesCriterios
      .filter((ac: any) => ac.comentario && ac.comentario.trim() !== '')
      .map((ac: any) => ({
        criterioId: ac.criterioId, // Backend pode querer comentario geral (sem criterioId) ou por criterio
        texto: ac.comentario.trim()
      }));

    const payload: AvaliacaoRequest = {
      professorId: this.professorId,
      cadeiraId: this.cadeiraId,
      periodo: formValue.periodo,
      notasCriterios: notasParaEnviar,
      comentarios: comentariosParaEnviar.length > 0 ? comentariosParaEnviar : undefined
    };

    console.log('Payload da Avaliação (AvaliacaoRequest):', payload);
    this.isLoading = true;
    this.avaliacaoSub = this.avaliacaoService.submitAvaliacao(payload).subscribe({
      next: (response) => {
        this.snackBar.open(response.message || 'Avaliação enviada com sucesso!', 'OK', { duration: 3000 });
        this.router.navigate(['/professor', this.professorId]);
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = 'Erro ao enviar avaliação. Tente novamente.';
        if (err.error && err.error.message) {
          this.errorMessage = err.error.message;
        } else if (err.message) {
            this.errorMessage = err.message;
        }
        this.snackBar.open(this.errorMessage || 'Ocorreu um erro desconhecido.', 'Fechar', { duration: 5000 });
        this.isLoading = false;
      }
    });
  }

  cancelarAvaliacao(): void {
    if (this.professorId) {
      this.router.navigate(['/professor', this.professorId]);
    } else {
      // Fallback se professorId não estiver disponível por algum motivo
      this.router.navigate(['/professores']); 
    }
  }

  ngOnDestroy(): void {
    this.routeSub?.unsubscribe();
    this.criteriosSub?.unsubscribe();
    this.professorSub?.unsubscribe();
    this.avaliacaoSub?.unsubscribe();
  }
} 