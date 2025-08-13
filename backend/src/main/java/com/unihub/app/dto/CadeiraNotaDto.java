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
public class CadeiraNotaDto {
    private Long cadeiraId;
    private String cadeiraNome;
    private String cursoNome;
    private Integer cargaHoraria;
    private Boolean isEletiva;
    private BigDecimal notaMedia;
    private Long totalAvaliacoes;
}
