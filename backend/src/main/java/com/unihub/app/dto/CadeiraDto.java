package com.unihub.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CadeiraDto {
    private Long id;
    private String nome;
    private Integer cargaHoraria;
    private Boolean isEletiva;

    @NotNull(message = "Curso ID não pode ser nulo")
    private Long cursoId;
    private String cursoNome; // To display in lists, etc.
} 