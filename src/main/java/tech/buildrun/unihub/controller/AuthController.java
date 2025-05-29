package tech.buildrun.unihub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.buildrun.unihub.dto.AuthRequest;
import tech.buildrun.unihub.dto.AuthResponse;
import tech.buildrun.unihub.service.AuthService;

/**
 * Controller para operações de autenticação (registro e login).
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para registrar um novo usuário.
     * @param request DTO com username e password.
     * @return ResponseEntity com o token JWT e status HTTP 201 Created.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Endpoint para autenticar um usuário existente.
     * @param request DTO com username e password.
     * @return ResponseEntity com o token JWT e status HTTP 200 OK.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}