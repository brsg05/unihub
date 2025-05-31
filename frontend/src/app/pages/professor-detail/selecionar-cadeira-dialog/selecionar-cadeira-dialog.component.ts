import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { Cadeira } from '../../../models/cadeira.model';
import { CommonModule } from '@angular/common';
import { MatListModule } from '@angular/material/list';
import { MatButtonModule } from '@angular/material/button';

export interface SelecionarCadeiraDialogData {
  cadeiras: Cadeira[];
}

@Component({
  selector: 'app-selecionar-cadeira-dialog',
  templateUrl: './selecionar-cadeira-dialog.component.html',
  styleUrls: ['./selecionar-cadeira-dialog.component.scss'],
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatListModule,
    MatButtonModule
  ]
})
export class SelecionarCadeiraDialogComponent {
  constructor(
    public dialogRef: MatDialogRef<SelecionarCadeiraDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: SelecionarCadeiraDialogData
  ) {}

  onCadeiraSelecionada(cadeira: Cadeira): void {
    this.dialogRef.close({ selectedCadeira: cadeira });
  }

  onCancelar(): void {
    this.dialogRef.close();
  }
} 