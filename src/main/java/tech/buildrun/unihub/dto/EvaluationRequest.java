package tech.buildrun.unihub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * DTO para requisições de criação de avaliação.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequest {
    private Integer score;

    private String commentText; // Opcional: texto do comentário associado à avaliação
}
