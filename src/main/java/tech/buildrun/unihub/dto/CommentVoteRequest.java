package tech.buildrun.unihub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisições de votação de comentário.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentVoteRequest {

    private Boolean up; // true para voto positivo, false para voto negativo
}