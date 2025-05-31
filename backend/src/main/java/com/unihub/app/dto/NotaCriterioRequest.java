package com.unihub.app.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotaCriterioRequest {
    @NotNull
    private Long criterioId;

    @NotNull
    @Min(1)
    @Max(5) // Ajuste conforme sua escala de notas
    private Integer nota;
} 