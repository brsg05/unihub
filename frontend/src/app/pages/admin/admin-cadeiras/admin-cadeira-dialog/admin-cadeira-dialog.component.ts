import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Cadeira } from '../../../../models/cadeira.model'; // Updated model

import { CommonModule } from '@angular/common';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatCheckboxModule } from '@angular/material/checkbox'; // Import MatCheckboxModule

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
    MatCheckboxModule // Add MatCheckboxModule to imports
  ],
  templateUrl: './admin-cadeira-dialog.component.html',
  styleUrls: ['./admin-cadeira-dialog.component.scss']
})
export class AdminCadeiraDialogComponent implements OnInit {
  cadeiraForm: FormGroup;
  isEditMode: boolean;

  constructor(
    public dialogRef: MatDialogRef<AdminCadeiraDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AdminCadeiraDialogData,
    private fb: FormBuilder
  ) {
    this.isEditMode = data.isEditMode;
    this.cadeiraForm = this.fb.group({
      nome: [data.cadeira?.nome || '', Validators.required],
      cargaHoraria: [data.cadeira?.cargaHoraria || null, [Validators.required, Validators.min(1)]],
      isEletiva: [data.cadeira?.isEletiva || false, Validators.required] // Default to false, can be discussed
    });
  }

  ngOnInit(): void {}

  onCancel(): void {
    this.dialogRef.close();
  }

  onSave(): void {
    if (this.cadeiraForm.valid) {
      const result: Partial<Cadeira> = {
        nome: this.cadeiraForm.value.nome,
        cargaHoraria: this.cadeiraForm.value.cargaHoraria,
        isEletiva: this.cadeiraForm.value.isEletiva
      };
      // If in edit mode, ensure the id is passed back if it exists
      if (this.isEditMode && this.data.cadeira && this.data.cadeira.id) {
        (result as Cadeira).id = this.data.cadeira.id;
      }
      this.dialogRef.close(result);
    } else {
      this.cadeiraForm.markAllAsTouched();
    }
  }
} 