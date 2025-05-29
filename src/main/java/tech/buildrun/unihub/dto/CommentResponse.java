package tech.buildrun.unihub.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;


/**
 * DTO para respostas de comentário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private UUID id;
    private String text;
    private UUID userId;
    private String username; // Nome de usuário do autor do comentário
    private Integer score; // Score calculado (votos positivos - negativos)
    private Integer positiveVotesCount;
    private Integer negativeVotesCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}