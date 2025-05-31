package com.unihub.app.dto;

import com.unihub.app.entity.ERole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRoleRequest {
    @NotNull
    private ERole role;
} 