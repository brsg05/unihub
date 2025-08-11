package com.unihub.app.dto;

import com.unihub.app.entity.Avaliacao;
import com.unihub.app.entity.ERole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Value;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Value
public class UserResponseDTO {
    Long id;
    String username;
    String email;
    ERole role;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    // Password is NOT included in the response DTO for security reasons

}