import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatDialogRef, MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatRippleModule } from '@angular/material/core';
import { CadeiraSimplificada } from '../../../models/cadeira.model';

export interface CadeiraSelectionDialogData {
  professorNome: string;
  cadeiras: CadeiraSimplificada[];
}

@Component({
  selector: 'app-cadeira-selection-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatRippleModule
  ],
  template: `
    <div class="cadeira-selection-dialog">
      <h2 mat-dialog-title>
        <mat-icon class="title-icon">rate_review</mat-icon>
        Avaliar Professor
      </h2>
      
      <div mat-dialog-content class="dialog-content">
        <p class="dialog-subtitle">
          Selecione a disciplina para avaliar 
          <strong>{{ data.professorNome }}</strong>:
        </p>
        
        <!-- Lista de cadeiras -->
        <div class="cadeira-list" *ngIf="data.cadeiras && data.cadeiras.length > 0">
          <div 
            *ngFor="let cadeira of data.cadeiras; trackBy: trackByCadeiraId" 
            class="cadeira-option"
            [class.selected]="selectedCadeira?.id === cadeira.id"
            (click)="selectCadeira(cadeira)"
            matRipple>
            
            <div class="cadeira-content">
              <mat-icon class="discipline-icon">subject</mat-icon>
              <div class="cadeira-info">
                <div class="cadeira-name">{{ cadeira.nome }}</div>
                <div class="cadeira-course" *ngIf="cadeira.curso">
                  {{ cadeira.curso.nome }}
                </div>
              </div>
              <mat-icon 
                class="selection-icon" 
                [class.selected]="selectedCadeira?.id === cadeira.id">
                {{ selectedCadeira?.id === cadeira.id ? 'radio_button_checked' : 'radio_button_unchecked' }}
              </mat-icon>
            </div>
          </div>
        </div>

        <!-- Estado vazio -->
        <div class="no-cadeiras" *ngIf="!data.cadeiras || data.cadeiras.length === 0">
          <mat-icon class="empty-icon">school_off</mat-icon>
          <h3>Nenhuma disciplina encontrada</h3>
          <p>Não há disciplinas disponíveis para este professor.</p>
        </div>
      </div>
      
      <div mat-dialog-actions class="dialog-actions">
        <button 
          mat-button 
          (click)="onCancel()" 
          class="cancel-button">
          Cancelar
        </button>
        <button 
          mat-raised-button 
          color="primary" 
          [disabled]="!selectedCadeira"
          (click)="onConfirm()"
          class="confirm-button">
          <mat-icon>star</mat-icon>
          Avaliar
        </button>
      </div>
    </div>
  `,
  styles: [`
    .cadeira-selection-dialog {
      min-width: 600px;
      max-width: 700px;
      font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
      padding: 0;
    }

    h2[mat-dialog-title] {
      color: #1976d2;
      font-weight: 600;
      margin-bottom: 0;
      display: flex;
      align-items: center;
      font-size: 28px;
      padding: 32px 32px 16px 32px;
    }

    .title-icon {
      margin-right: 12px;
      font-size: 28px;
      width: 28px;
      height: 28px;
    }

    .dialog-content {
      padding: 16px 32px 24px 32px;
      max-height: 400px;
      overflow-y: auto;
    }

    .dialog-subtitle {
      margin-bottom: 32px;
      color: #666;
      line-height: 1.6;
      font-size: 18px;
      text-align: center;
    }

    .dialog-subtitle strong {
      color: #1976d2;
      font-weight: 600;
    }

    .cadeira-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .cadeira-option {
      border: 2px solid #e0e0e0;
      border-radius: 12px;
      padding: 20px;
      cursor: pointer;
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      background: #fff;
      box-shadow: 0 2px 8px rgba(0,0,0,0.04);
    }

    .cadeira-option:hover {
      border-color: #1976d2;
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(25, 118, 210, 0.15);
    }

    .cadeira-option.selected {
      border-color: #1976d2;
      background: linear-gradient(135deg, #e3f2fd 0%, #f8fcff 100%);
      box-shadow: 0 4px 16px rgba(25, 118, 210, 0.2);
    }

    .cadeira-content {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .discipline-icon {
      color: #1976d2;
      font-size: 32px;
      width: 32px;
      height: 32px;
      flex-shrink: 0;
    }

    .cadeira-info {
      flex: 1;
    }

    .cadeira-name {
      font-size: 18px;
      font-weight: 600;
      color: #333;
      line-height: 1.3;
      margin-bottom: 4px;
    }

    .cadeira-course {
      font-size: 14px;
      color: #757575;
      font-weight: 500;
    }

    .selection-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: #e0e0e0;
      transition: color 0.3s ease;
    }

    .selection-icon.selected {
      color: #1976d2;
    }

    .no-cadeiras {
      text-align: center;
      padding: 48px 24px;
      color: #757575;
    }

    .empty-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #e0e0e0;
      margin-bottom: 16px;
    }

    .no-cadeiras h3 {
      font-size: 20px;
      color: #666;
      margin: 16px 0 8px 0;
      font-weight: 500;
    }

    .no-cadeiras p {
      font-size: 16px;
      color: #999;
      margin: 0;
    }

    .dialog-actions {
      padding: 24px 32px 32px 32px;
      justify-content: flex-end;
      gap: 16px;
      background: #fafafa;
      border-top: 1px solid #e0e0e0;
    }

    .cancel-button {
      min-width: 120px;
      height: 44px;
      font-size: 16px;
      font-weight: 500;
      color: #666;
    }

    .confirm-button {
      min-width: 140px;
      height: 44px;
      font-size: 16px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .confirm-button[disabled] {
      background: #e0e0e0 !important;
      color: #999 !important;
    }

    .confirm-button mat-icon {
      font-size: 20px;
      width: 20px;
      height: 20px;
    }

    @media (max-width: 600px) {
      .cadeira-selection-dialog {
        min-width: 340px;
        max-width: 95vw;
      }
      
      h2[mat-dialog-title] {
        font-size: 24px;
        padding: 24px 24px 12px 24px;
      }
      
      .dialog-content {
        padding: 12px 24px 20px 24px;
      }
      
      .dialog-actions {
        padding: 20px 24px 24px 24px;
        flex-direction: column;
        gap: 12px;
      }
      
      .cancel-button,
      .confirm-button {
        width: 100%;
      }
      
      .cadeira-option {
        padding: 16px;
      }
    }
  `]
})
export class CadeiraSelectionDialogComponent {
  selectedCadeira: CadeiraSimplificada | null = null;

  constructor(
    public dialogRef: MatDialogRef<CadeiraSelectionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: CadeiraSelectionDialogData
  ) {}

  selectCadeira(cadeira: CadeiraSimplificada): void {
    this.selectedCadeira = cadeira;
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onConfirm(): void {
    if (this.selectedCadeira) {
      this.dialogRef.close(this.selectedCadeira);
    }
  }

  trackByCadeiraId(index: number, item: CadeiraSimplificada): number {
    return item.id;
  }
}
