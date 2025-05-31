import { User } from './user.model';
import { Professor } from './professor.model';
import { Cadeira } from './cadeira.model';
import { NotaCriterio } from './nota-criterio.model';
import { Comentario, ComentarioPublicDto } from './comentario.model';
import { Criterio } from './criterio.model';

export interface Avaliacao {
  id: number;
  data: Date; // or string
  periodo: string;
  userId: number;
  professorId: number;
  cadeiraId: number;
  // notasCriterios: NotaCriterio[]; // Populated on detail, or part of request
  // comentarios?: Comentario[];    // Populated on detail, or part of request
}

// For submitting a new evaluation (AvaliacaoRequest on backend)
export interface AvaliacaoRequest {
  professorId: number;
  cadeiraId: number;
  periodo: string;
  notasCriterios: { criterioId: number; nota: number }[];
  comentarios?: { criterioId?: number; texto: string }[]; // Optional comments, can be general or per criterio
}

// For public display of an evaluation (AvaliacaoPublicDto on backend)
export interface AvaliacaoPublicDto {
  id: number;
  data: string; // ISO Date string
  periodo: string;
  professorNome: string;
  cadeiraNome: string;
  notas: AvaliacaoNotaPublicDto[];
  comentarios: ComentarioPublicDto[];
  // Potentially overall average for this specific evaluation if useful
}

export interface AvaliacaoNotaPublicDto {
  criterioNome: string;
  nota: number;
}

export interface AvaliacaoCriterioPayload {
  criterioId: number;
  nota: number;
  comentario?: string;
}

export interface AvaliacaoPayload {
  professorId: number;
  cadeiraId: number;
  avaliacoes: AvaliacaoCriterioPayload[];
} 