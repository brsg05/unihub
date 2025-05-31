package com.unihub.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ComentarioPublicDto {
    private Long id;
    private String texto;
    private String criterioNome;
    private Integer score;
    private LocalDateTime createdAt;
} 