package tech.buildrun.unihub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para respostas de avaliação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationResponse {
    private UUID id;
    private Integer score;
    private UUID userId;
    private UUID professorId;
    private UUID criterionId;
    private UUID commentId; // ID do comentário, se houver
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}