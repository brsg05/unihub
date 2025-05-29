package tech.buildrun.unihub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set; /**
     * DTO para requisições de criação/atualização de usuário (Admin-only).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class UserRequest {

        private String username;

        private String password;

        private String email;

        // Nomes das roles, ex: ["ROLE_USER", "ROLE_ADMIN"]
        private Set<String> roles;
    }
