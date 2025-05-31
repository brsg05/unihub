package com.unihub.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CadeiraRequest {
    @NotBlank
    @Size(max = 150)
    private String nome;

    @NotNull
    @Positive
    private Integer cargaHoraria;

    @NotNull
    private Boolean isEletiva;
} 