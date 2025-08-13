export interface Cadeira {
  id: number;
  nome: string;
  codigo?: string;
  cargaHoraria: number;
  isEletiva: boolean;
  cursoId: number;
  cursoNome?: string;
  descricao?: string;
  professorCount?: number;
  notaGeral?: number;
} 

// Interface simplificada da cadeira
export interface CadeiraSimplificada {
  id: number;
  nome: string;
  curso?: {
    id: number;
    nome: string;
  };
} 