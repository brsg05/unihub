package com.unihub.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CursoDto {
    private Long id;

    @NotBlank(message = "Nome do curso não pode ser em branco")
    @Size(max = 150, message = "Nome do curso deve ter no máximo 150 caracteres")
    private String nome;

    private List<CadeiraSimplificadaDto> cadeiras; // For displaying cadeiras within a curso, if needed

    public CursoDto(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }
} 