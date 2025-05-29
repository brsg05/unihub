package tech.buildrun.unihub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
     * DTO para requisições de criação/atualização de professor.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class ProfessorRequest {

        private String name;
        private String email;
        private String department;
    }
