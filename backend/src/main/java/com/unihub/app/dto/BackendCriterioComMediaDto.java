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
public class BackendCriterioComMediaDto {
    private BackendCriterioDto criterio;
    private BigDecimal mediaNotas;
    private ComentarioSimplificadoDto topComentario; // Mantém o ComentarioSimplificadoDto que você já tem
} 