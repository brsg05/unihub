package com.unihub.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CriterioRequest {
    @NotBlank
    @Size(max = 100)
    private String nome;
} 