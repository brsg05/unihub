//package com.unihub.app.controller;
//
//import com.unihub.app.dto.JwtResponse;
//import com.unihub.app.dto.LoginRequest;
//import com.unihub.app.dto.MessageResponse;
//import com.unihub.app.dto.RegisterRequest;
//import com.unihub.app.service.AuthService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.responses.ApiResponses;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@CrossOrigin(origins = "*", maxAge = 3600)
//@RestController
//@RequestMapping("/api/auth")
//@Tag(name = "Autenticação", description = "Endpoints para registro e login de usuários")
//public class AuthController {
//
//    @Autowired
//    AuthService authService;
//
//    @Operation(summary = "Autentica um usuário", description = "Realiza o login do usuário e retorna um token JWT.")
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "Login bem-sucedido"),
//        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
//    })
//    @PostMapping("/login")
//    public ResponseEntity<JwtResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
//        JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
//        return ResponseEntity.ok(jwtResponse);
//    }
//
//    @Operation(summary = "Registra um novo usuário", description = "Cria uma nova conta de usuário no sistema.")
//    @ApiResponses(value = {
//        @ApiResponse(responseCode = "200", description = "Usuário registrado com sucesso"),
//        @ApiResponse(responseCode = "400", description = "Dados de registro inválidos ou usuário/email já existente")
//    })
//    @PostMapping("/register")
//    public ResponseEntity<MessageResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
//        authService.registerUser(registerRequest);
//        return ResponseEntity.ok(new MessageResponse("Usuário registrado com sucesso!"));
//    }
//}