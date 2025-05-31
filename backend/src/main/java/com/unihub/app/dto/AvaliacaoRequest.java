package com.unihub.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AvaliacaoRequest {
    @NotNull
    private Long professorId;

    @NotNull
    private Long cadeiraId;

    @NotBlank
    @Size(max = 10)
    private String periodo;

    @NotEmpty
    @Valid
    private List<NotaCriterioRequest> notasCriterios;

    @Valid // Comentários são opcionais, mas se presentes, devem ser válidos
    private List<ComentarioRequest> comentarios;
} 