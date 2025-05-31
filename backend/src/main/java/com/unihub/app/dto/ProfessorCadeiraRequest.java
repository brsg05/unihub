package com.unihub.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfessorCadeiraRequest {
    @NotNull
    private Long cadeiraId;
} 