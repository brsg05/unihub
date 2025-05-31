import { ComentarioPublicDto } from './comentario.model';

// Represents a specific grade and comment for a criterion in the context of an evaluation
export interface NotaComentarioCriterioDto {
  nota: number;
  comentario?: ComentarioPublicDto | null; // Comment specific to this criterion in this evaluation
}

// Represents an evaluation instance focused on a specific criterion for a professor
export interface AvaliacaoParaCriterioPublicDto {
  idAvaliacao: number;
  dataAvaliacao: string; // ISO date string
  periodoAvaliacao: string; // e.g., "2023.1"
  nomeCadeira?: string; // Optional: Name of the course if relevant to display
  notaCriterio: NotaComentarioCriterioDto;
} 