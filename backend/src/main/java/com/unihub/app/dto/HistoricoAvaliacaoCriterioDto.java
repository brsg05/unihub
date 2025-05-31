package com.unihub.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoAvaliacaoCriterioDto {
    private Long avaliacaoId;
    private String periodo;
    private Integer nota;
    private ComentarioSimplificadoDto comentario; // Comentário associado àquela nota específica
} 