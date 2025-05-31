import { ComentarioPublic } from './comentario-public.model';
import { AvaliacaoNotaPublic } from './avaliacao-nota-public.model';

export interface AvaliacaoPublic {
  id: number;
  data: string; // Or Date/LocalDateTime if conversion is handled
  periodo: string;
  professorNome?: string; // May not be needed if already in page context
  cadeiraNome?: string; // May not be needed if already in page context
  // For the specific criterion page, 'notas' might be just one note or not present if comments are the focus.
  notas?: AvaliacaoNotaPublic[]; 
  comentarios: ComentarioPublic[];
  // Other fields from backend AvaliacaoPublicDto as needed
} 