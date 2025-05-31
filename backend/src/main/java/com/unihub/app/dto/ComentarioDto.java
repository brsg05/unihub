package com.unihub.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ComentarioDto {
    private Long id;
    private String texto;
    private Long avaliacaoId;
    private Long criterioId;
    private String criterioNome; // Adicionado para conveniência
    private Integer votosPositivos;
    private Integer votosNegativos;
    private Integer score;
    private LocalDateTime createdAt;
} 