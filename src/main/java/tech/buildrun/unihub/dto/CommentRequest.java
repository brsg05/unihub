package tech.buildrun.unihub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * DTO para requisições de criação/atualização de comentário (não usado diretamente para criação,
 * o comentário é criado junto com a avaliação ou não é atualizável).
 * Pode ser usado para validação ou representação interna.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
   private String text;
}
