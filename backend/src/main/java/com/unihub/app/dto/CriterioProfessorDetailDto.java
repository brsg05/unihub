package com.unihub.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CriterioProfessorDetailDto {
    private Long criterioId;
    private String criterioNome;
    private Long professorId;
    private String professorNome;
    private List<HistoricoAvaliacaoCriterioDto> historicoAvaliacoes;
    private List<ComentarioSimplificadoDto> principaisComentarios;
} 