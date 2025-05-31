package com.unihub.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvaliacaoNotaPublicDto {
    private String criterioNome;
    private Integer nota;
} 