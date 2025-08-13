import { Cadeira } from './cadeira.model';
import { ComentarioPublicDto } from './comentario.model';
import { Criterio } from './criterio.model';

export interface Professor {
  id: number;
  nomeCompleto: string;
  photoUrl?: string;
  cadeiras?: Cadeira[]; // May not always be loaded
  notaGeral?: number; // Derived
}

// Interface para notas por cadeira - corresponde ao CadeiraNotaDto do backend
export interface CadeiraNota {
  cadeiraId: number;
  cadeiraNome: string;
  cursoNome: string;
  cargaHoraria: number;
  isEletiva: boolean;
  notaMedia: number;
  totalAvaliacoes: number;
}

// Interface simplificada da cadeira
export interface CadeiraSimplificada {
  id: number;
  nome: string;
}

// Interface para critério com comentário simplificado
export interface ComentarioSimplificado {
  id: number;
  texto: string;
  score: number;
}

export interface BackendCriterio {
  id: number;
  nome: string;
}

export interface BackendCriterioComMedia {
  criterio: BackendCriterio;
  mediaNotas: number;
  topComentario?: ComentarioSimplificado;
}

// Corresponds to backend ProfessorDto (summary view)
export interface ProfessorDto {
  id: number;
  nomeCompleto: string;
  photoUrl?: string;
  notaGeral?: number;
  // Add other summary fields if your backend DTO has them (e.g., number of evaluations)
}

// Corresponds to backend ProfessorRequest (for create/update)
export interface ProfessorRequest {
  nomeCompleto: string;
  photoUrl?: string;
  cadeiraIds: number[];
}

// Corresponds to backend ProfessorDetailDto (detailed view for professor page)
export interface ProfessorDetailDto extends ProfessorDto {
  cadeiras: CadeiraSimplificada[];
  cadeiraNotas: CadeiraNota[]; // Notas por cadeira
  criteriosComMedias: BackendCriterioComMedia[];
}

export interface CriterioComMediaDto {
  criterio: Criterio;
  mediaNotas: number;
  // topComentario?: Comentario; // Or a simplified Comentario DTO
}

// For paginated responses (generic, can be reused)
export interface Page<T> {
  content: T[];
  pageable: {
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    pageNumber: number;
    pageSize: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
} 