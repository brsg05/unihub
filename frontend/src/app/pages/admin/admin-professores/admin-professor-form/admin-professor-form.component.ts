import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';

import { ProfessorService } from '../../../../core/services/professor.service';
import { CadeiraService } from '../../../../core/services/cadeira.service';
import { ProfessorDto, ProfessorRequest, ProfessorDetailDto } from '../../../../models/professor.model';
import { Cadeira } from '../../../../models/cadeira.model';
import { Observable, of } from 'rxjs';
import { switchMap, tap } from 'rxjs/operators';

export interface AdminProfessorFormData {
  professor: ProfessorDto | null;
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
    MatButtonModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatIconModule
  ],
  templateUrl: './admin-professor-form.component.html',
  styleUrls: ['./admin-professor-form.component.scss']
})
export class AdminProfessorFormComponent implements OnInit {
  professorForm: FormGroup;
  isEditMode = false;
  isLoading = true;
  allCadeiras$: Observable<Cadeira[]>;
  private professorIdToEdit: number | null = null;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<AdminProfessorFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AdminProfessorFormData,
    private professorService: ProfessorService,
    private cadeiraService: CadeiraService,
    private snackBar: MatSnackBar
  ) {
    this.isEditMode = !!this.data.professor;
    if (this.isEditMode && this.data.professor) {
      this.professorIdToEdit = this.data.professor.id;
    }

    this.professorForm = this.fb.group({
      nomeCompleto: ['', Validators.required],
      photoUrl: [''],
      cadeiraIds: [[]]
    });
    this.allCadeiras$ = this.cadeiraService.getAllCadeirasList();
  }

  ngOnInit(): void {
    if (this.isEditMode && this.professorIdToEdit !== null) {
      this.isLoading = true;
      this.professorService.getProfessorById(this.professorIdToEdit).subscribe({
        next: (professorDetail: ProfessorDetailDto) => {
          this.professorForm.patchValue({
            nomeCompleto: professorDetail.nomeCompleto,
            photoUrl: professorDetail.photoUrl,
            cadeiraIds: professorDetail.cadeiras?.map((c: Cadeira) => c.id) || []
          });
          this.isLoading = false;
        },
        error: (err) => {
          this.snackBar.open('Erro ao carregar dados do professor para edição.', 'Fechar', { duration: 3000 });
          this.isLoading = false;
          this.dialogRef.close();
        }
      });
    } else {
      this.isLoading = false;
    }
  }

  onSave(): void {
    if (this.professorForm.invalid) {
      this.professorForm.markAllAsTouched();
      this.snackBar.open('Por favor, corrija os erros no formulário.', 'Fechar', { duration: 3000 });
      return;
    }

    this.isLoading = true;
    const formValue = this.professorForm.value;
    const professorRequest: ProfessorRequest = {
      nomeCompleto: formValue.nomeCompleto,
      photoUrl: formValue.photoUrl || undefined,
      cadeiraIds: formValue.cadeiraIds || []
    };

    let saveObservable: Observable<ProfessorDto | ProfessorDetailDto>;

    if (this.isEditMode && this.professorIdToEdit !== null) {
      saveObservable = this.professorService.updateProfessor(this.professorIdToEdit, professorRequest);
    } else {
      saveObservable = this.professorService.createProfessor(professorRequest);
    }

    saveObservable.subscribe({
      next: () => {
        this.snackBar.open(`Professor ${this.isEditMode ? 'atualizado' : 'criado'} com sucesso!`, 'OK', { duration: 3000 });
        this.isLoading = false;
        this.dialogRef.close(true);
      },
      error: (err: any) => {
        this.isLoading = false;
        const defaultMessage = `Erro ao ${this.isEditMode ? 'atualizar' : 'criar'} professor.`;
        this.snackBar.open(err?.error?.message || defaultMessage, 'Fechar', { duration: 5000 });
        console.error(err);
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }
} 