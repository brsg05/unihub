package com.unihub.app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AvaliacaoDto {
    private Long id;
    private LocalDateTime dataAvaliacao;
    private String periodo;
    // private Long usuarioId; // Geralmente não exposto publicamente
    private Long professorId;
    private Long cadeiraId;
    private List<NotaCriterioDto> notasCriterios;
    private List<ComentarioDto> comentarios;
} 