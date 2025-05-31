package com.unihub.app.controller;

import com.unihub.app.dto.UpdateUserRoleRequest;
import com.unihub.app.dto.UserDto;
import com.unihub.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários (ADMIN)")
@SecurityRequirement(name = "bearerAuth") // Indica que os endpoints aqui requerem autenticação Bearer
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Lista todos os usuários", description = "Retorna uma lista de todos os usuários cadastrados. Requer papel ADMIN.")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Busca usuário por ID", description = "Retorna os detalhes de um usuário específico. Requer papel ADMIN.")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza o papel de um usuário", description = "Altera o papel (role) de um usuário. Requer papel ADMIN.")
    public ResponseEntity<UserDto> updateUserRole(@PathVariable Long id, @Valid @RequestBody UpdateUserRoleRequest roleRequest) {
        return ResponseEntity.ok(userService.updateUserRole(id, roleRequest));
    }
} 