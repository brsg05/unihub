package com.unihub.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
// Importar o novo DTO que será criado
// import com.unihub.app.dto.BackendCriterioComMediaDto;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorDetailDto {
    private Long id;
    private String nomeCompleto;
    private String photoUrl;
    private BigDecimal notaGeral;
    private List<CadeiraSimplificadaDto> cadeiras; // Renomeado de cadeirasLecionadas
    private List<CadeiraNotaDto> cadeiraNotas; // Nova propriedade para as notas por cadeira
    private List<BackendCriterioComMediaDto> criteriosComMedias; // Alterado de avaliacoesPorCriterio e usa novo DTO
} 