package tech.buildrun.unihub.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
     * DTO para respostas de usuário.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class UserResponse {
        private UUID id;
        private String username;
        private String email;
        private Set<String> roles; // Nomes das roles
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }




