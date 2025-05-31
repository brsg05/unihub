package com.unihub.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComentarioRequest {
    @NotNull
    private Long criterioId;

    @NotBlank
    private String texto;
} 