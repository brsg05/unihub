package com.unihub.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorDto {
    private Long id;
    private String nomeCompleto;
    private String photoUrl;
    private BigDecimal notaGeral;
    private Set<CadeiraDto> cadeiras;
} 