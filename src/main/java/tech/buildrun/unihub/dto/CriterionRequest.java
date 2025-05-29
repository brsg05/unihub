package tech.buildrun.unihub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
 * DTO para requisições de criação/atualização de critério.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriterionRequest {

    private String name;

    private String description;
}
