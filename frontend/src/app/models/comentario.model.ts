import { User } from './user.model';
import { Criterio } from './criterio.model';

export interface Comentario {
  id: number;
  texto: string;
  data: Date; // Or string, if you prefer to handle date conversion manually
  votosPositivos: number;
  votosNegativos: number;
  // user?: User; // Usually not exposed publicly, or only username
  criterio?: Criterio; // The specific Criterio this comment might be for, if applicable
  // avaliacaoId?: number; // Link back to the evaluation
}

// DTO for public view, might be slightly different (e.g. no full user object)
export interface ComentarioPublicDto {
  id: number;
  texto: string;
  data: string; // Backend usually sends dates as ISO strings
  votosPositivos: number;
  votosNegativos: number;
  score: number; // Calculated (votosPositivos - votosNegativos)
  criterioNome?: string;
  // Add other fields as needed from your backend DTO
} 