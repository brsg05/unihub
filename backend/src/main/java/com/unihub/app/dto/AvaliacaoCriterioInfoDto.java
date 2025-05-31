package com.unihub.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoCriterioInfoDto {
    private Long criterioId;
    private String criterioNome;
    private BigDecimal notaMedia;
    private ComentarioSimplificadoDto principalComentario; // Comentário com maior score
} 