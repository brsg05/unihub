package com.unihub.app.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotaCriterioDto {
    private Long id;
    private CriterioDto criterio;
    private Integer nota;
} 