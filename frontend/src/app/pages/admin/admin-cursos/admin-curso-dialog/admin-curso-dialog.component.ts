import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Curso } from '../../../../models/curso.model';

export interface AdminCursoDialogData {
  isEdit: boolean;
  curso?: Curso;
}

@Component({
  selector: 'app-admin-curso-dialog',
  templateUrl: './admin-curso-dialog.component.html',
  // styleUrls: ['./admin-curso-dialog.component.scss'] // No custom styles needed for now
})
export class AdminCursoDialogComponent implements OnInit {
  cursoForm!: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<AdminCursoDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: AdminCursoDialogData,
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.cursoForm = this.fb.group({
      // The dialog only returns the name, ID is handled by the service/backend
      nome: [this.data.curso?.nome || '', [Validators.required, Validators.maxLength(150)]]
    });
  }

  onNoClick(): void {
    this.dialogRef.close();
  }
} 