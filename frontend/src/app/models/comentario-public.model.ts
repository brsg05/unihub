export interface ComentarioPublic {
  id: number;
  texto: string;
  criterioNome: string; // Should match the current criterion page
  score: number;
  createdAt: string; // Or Date/LocalDateTime
  // Add other fields like upvotes/downvotes if available and needed
} 