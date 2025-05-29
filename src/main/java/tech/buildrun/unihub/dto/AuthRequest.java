package tech.buildrun.unihub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; /**
     * DTO para requisições de autenticação (login e registro).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class AuthRequest {
        private String username;
        private String password;
    }
