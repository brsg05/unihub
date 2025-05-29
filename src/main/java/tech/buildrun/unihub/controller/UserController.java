package tech.buildrun.unihub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.buildrun.unihub.dto.UserRequest;
import tech.buildrun.unihub.dto.UserResponse;
import tech.buildrun.unihub.service.UserService;

import java.util.List;
import java.util.UUID;

/**
 * Controller para gerenciamento de usuários (apenas para ADMIN).
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Todas as operações neste controller requerem ROLE_ADMIN
public class UserController {

    private final UserService userService;

    /**
     * Obtém todos os usuários.
     * @return Lista de UserResponse.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * Obtém um usuário por ID.
     * @param id ID do usuário.
     * @return UserResponse.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * Cria um novo usuário.
     * @param request DTO com dados do usuário.
     * @return UserResponse do usuário criado.
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        UserResponse newUser = userService.createUser(request);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    /**
     * Atualiza um usuário existente.
     * @param id ID do usuário a ser atualizado.
     * @param request DTO com dados atualizados.
     * @return UserResponse do usuário atualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        UserResponse updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deleta um usuário.
     * @param id ID do usuário a ser deletado.
     * @return ResponseEntity com status HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
