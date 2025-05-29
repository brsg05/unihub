package tech.buildrun.unihub.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
     * DTO para respostas de professor, incluindo médias e comentários.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public class ProfessorResponse {
        private UUID id;
        private String name;
        private String email;
        private String department;
        private Double averageScore; // Média geral de todas as avaliações
        private Map<String, Double> criteriaAverages; // Média por critério (Nome do Critério -> Média)
        private List<CommentResponse> topComments; // Comentários "top" (maior score) por critério
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

