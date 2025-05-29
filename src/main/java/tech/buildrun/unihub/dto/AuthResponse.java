package tech.buildrun.unihub.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
     * DTO para respostas de autenticação, contendo o token JWT.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class AuthResponse {
        private String token;
    }

