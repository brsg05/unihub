package com.unihub.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AvaliacaoPublicDto {
    private Long id;
    private String data;
    private String periodo;
    private String professorNome;
    private String cadeiraNome;
    private List<AvaliacaoNotaPublicDto> notas;
    private List<ComentarioPublicDto> comentarios;
} 