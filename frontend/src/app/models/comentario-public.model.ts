export interface ComentarioPublic {
  id: number;
  texto: string;
  criterioNome: string; // Should match the current criterion page
  score: number;
  createdAt: string; // Or Date/LocalDateTime
  upvotes?: number;
  downvotes?: number;
  userVote?: 'up' | 'down' | null;
} 