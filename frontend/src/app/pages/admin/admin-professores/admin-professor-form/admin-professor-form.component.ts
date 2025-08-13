import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';

import { Professor } from '../../../../models/professor.model';
import { CadeiraSimplificada } from '../../../../models/cadeira.model';
import { ProfessorService } from '../../../../services/professor.service';
import { CadeiraService } from '../../../../services/cadeira.service';

export interface AdminProfessorFormData {
  professor?: Professor;
  cadeiras: CadeiraSimplificada[];
}

@Component({
  selector: 'app-admin-professor-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatCheckboxModule
  ],
  template: `
    <div class="admin-professor-form-container">
      <h2 mat-dialog-title>{{ isEditing ? 'Editar Professor' : 'Novo Professor' }}</h2>
      
      <form [formGroup]="professorForm" (ngSubmit)="onSubmit()">
        <mat-form-field appearance="outline" class="form-field">
          <mat-label>Nome</mat-label>
          <input matInput formControlName="nome" placeholder="Nome completo do professor">
          <mat-error *ngIf="professorForm.get('nome')?.hasError('required')">
            Nome é obrigatório
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="form-field">
          <mat-label>Email</mat-label>
          <input matInput formControlName="email" type="email" placeholder="email@exemplo.com">
          <mat-error *ngIf="professorForm.get('email')?.hasError('required')">
            Email é obrigatório
          </mat-error>
          <mat-error *ngIf="professorForm.get('email')?.hasError('email')">
            Email deve ser válido
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="form-field">
          <mat-label>Departamento</mat-label>
          <input matInput formControlName="departamento" placeholder="Departamento">
          <mat-error *ngIf="professorForm.get('departamento')?.hasError('required')">
            Departamento é obrigatório
          </mat-error>
        </mat-form-field>

        <mat-form-field appearance="outline" class="form-field">
          <mat-label>Cadeiras</mat-label>
          <mat-select formControlName="cadeiraIds" multiple>
            <mat-option *ngFor="let cadeira of data.cadeiras" [value]="cadeira.id">
              {{ cadeira.nome }} ({{ cadeira.curso?.nome }})
            </mat-option>
          </mat-select>
        </mat-form-field>

        <mat-checkbox formControlName="ativo" class="form-field">
          Professor Ativo
        </mat-checkbox>

        <div class="form-actions" mat-dialog-actions>
          <button mat-button type="button" (click)="onCancel()">Cancelar</button>
          <button mat-raised-button color="primary" type="submit" [disabled]="!professorForm.valid || isLoading">
            {{ isLoading ? 'Salvando...' : (isEditing ? 'Atualizar' : 'Criar') }}
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [`
    .admin-professor-form-container {
      padding: 20px;
      min-width: 400px;
    }

    .form-field {
      width: 100%;
      margin-bottom: 16px;
    }

    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      margin-top: 20px;
      padding: 0;
    }
  `]
})
export class AdminProfessorFormComponent implements OnInit {
  @Input() data: AdminProfessorFormData = { cadeiras: [] };
  @Output() professorSaved = new EventEmitter<Professor>();
  @Output() cancelled = new EventEmitter<void>();

  professorForm: FormGroup;
  isEditing = false;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private professorService: ProfessorService,
    private cadeiraService: CadeiraService,
    private dialogRef?: MatDialogRef<AdminProfessorFormComponent>
  ) {
    this.professorForm = this.fb.group({
      nome: ['', [Validators.required]],
      email: ['', [Validators.required, Validators.email]],
      departamento: ['', [Validators.required]],
      cadeiraIds: [[]],
      ativo: [true]
    });
  }

  ngOnInit(): void {
    if (this.data.professor) {
      this.isEditing = true;
      this.loadProfessorData();
    }
  }

  private loadProfessorData(): void {
    const professor = this.data.professor;
    if (professor) {
      this.professorForm.patchValue({
        nome: professor.nomeCompleto,
        email: professor.photoUrl, // Placeholder - adjust based on your actual fields
        departamento: 'Departamento', // Placeholder - adjust based on your actual fields
        cadeiraIds: professor.cadeiras?.map(c => c.id) || [],
        ativo: true // Placeholder - adjust based on your actual fields
      });
    }
  }

  onSubmit(): void {
    if (this.professorForm.valid && !this.isLoading) {
      this.isLoading = true;
      
      const formValue = this.professorForm.value;
      const professorData: Partial<Professor> = {
        nomeCompleto: formValue.nome,
        photoUrl: formValue.email // Adjust this mapping based on your actual fields
      };

      const operation = this.isEditing && this.data.professor?.id
        ? this.professorService.updateProfessor(this.data.professor.id, professorData)
        : this.professorService.createProfessor(professorData);

      operation.subscribe({
        next: (professor) => {
          this.professorSaved.emit(professor);
          this.dialogRef?.close(professor);
          this.isLoading = false;
        },
        error: (error) => {
          console.error('Erro ao salvar professor:', error);
          this.isLoading = false;
        }
      });
    }
  }

  onCancel(): void {
    this.cancelled.emit();
    this.dialogRef?.close();
  }
}
