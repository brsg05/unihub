import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Cadeira } from '../../../../models/cadeira.model';
import { Curso } from '../../../../models/curso.model'; // Import Curso model
import { CursoService } from '../../../../services/curso.service'; // Import CursoService
import { Observable } from 'rxjs'; // Import Observable

import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select'; // Import MatSelectModule

export interface AdminCadeiraDialogData {
  cadeira: Partial<Cadeira> | null;
  isEditMode: boolean;
}

@Component({
  selector: 'app-admin-cadeira-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    MatCheckboxModule,
    MatSelectModule // Add MatSelectModule to imports
  ],
  templateUrl: './admin-cadeira-dialog.component.html',
  styleUrls: ['./admin-cadeira-dialog.component.scss']
})
export class AdminCadeiraDialogComponent implements OnInit {
  cadeiraForm: FormGroup;
  isEditMode: boolean;
  cursos$!: Observable<Curso[]>; // Observable for Cursos list

  constructor(
    public dialogRef: MatDialogRef<AdminCadeiraDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AdminCadeiraDialogData,
    private fb: FormBuilder,
    private cursoService: CursoService // Inject CursoService
  ) {
    this.isEditMode = data.isEditMode;
    this.cadeiraForm = this.fb.group({
      nome: [data.cadeira?.nome || '', Validators.required],
      cargaHoraria: [data.cadeira?.cargaHoraria || null, [Validators.required, Validators.min(1)]],
      isEletiva: [data.cadeira?.isEletiva || false, Validators.required],
      cursoId: [data.cadeira?.cursoId || null, Validators.required] // Add cursoId to form
    });
  }

  ngOnInit(): void {
    this.cursos$ = this.cursoService.getAllCursos(); // Load cursos
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.cadeiraForm.valid) {
      const formValue = this.cadeiraForm.value;
      const result: Partial<Cadeira> = {
        nome: formValue.nome,
        cargaHoraria: formValue.cargaHoraria,
        isEletiva: formValue.isEletiva,
        cursoId: formValue.cursoId // Include cursoId in result
      };
      if (this.isEditMode && this.data.cadeira && this.data.cadeira.id) {
        (result as Cadeira).id = this.data.cadeira.id;
      }
      this.dialogRef.close(result);
    } else {
      this.cadeiraForm.markAllAsTouched();
    }
  }
} 