export interface Cadeira {
  id: number;
  nome: string;
  cargaHoraria: number;
  isEletiva: boolean;
  cursoId: number;
  cursoNome?: string; // Optional: primarily for display in lists
} 