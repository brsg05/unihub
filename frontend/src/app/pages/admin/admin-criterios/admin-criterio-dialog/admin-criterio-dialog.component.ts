import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Criterio } from '../../../../models/criterio.model';

import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';

export interface AdminCriterioDialogData {
  criterio: Partial<Criterio> | null; // Partial for create, Criterio for edit
  isEditMode: boolean;
}

@Component({
  selector: 'app-admin-criterio-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule
  ],
  templateUrl: './admin-criterio-dialog.component.html',
  styleUrls: ['./admin-criterio-dialog.component.scss']
})
export class AdminCriterioDialogComponent implements OnInit {
  criterioForm: FormGroup;
  isEditMode: boolean;

  constructor(
    public dialogRef: MatDialogRef<AdminCriterioDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AdminCriterioDialogData,
    private fb: FormBuilder
  ) {
    this.isEditMode = data.isEditMode;
    this.criterioForm = this.fb.group({
      nome: [data.criterio?.nome || '', Validators.required]
    });
  }

  ngOnInit(): void {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.criterioForm.valid) {
      // For edit mode, we might want to merge with existing id if not part of form
      const result: Partial<Criterio> = {
        // id: this.isEditMode && this.data.criterio ? this.data.criterio.id : undefined,
        nome: this.criterioForm.value.nome
      };
      this.dialogRef.close(result);
    } else {
      this.criterioForm.markAllAsTouched(); // Show validation errors
    }
  }
} 