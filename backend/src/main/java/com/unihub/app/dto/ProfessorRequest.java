package com.unihub.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.util.Set;

@Getter
@Setter
public class ProfessorRequest {
    @NotBlank
    @Size(max = 255)
    private String nomeCompleto;

    @Size(max = 500)
    @URL
    private String photoUrl;

    private Set<Long> cadeiraIds; // IDs das cadeiras a serem associadas
} 