package tech.buildrun.unihub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.buildrun.unihub.dto.AuthRequest;
import tech.buildrun.unihub.dto.AuthResponse;
import tech.buildrun.unihub.entity.Role;
import tech.buildrun.unihub.entity.User;
import tech.buildrun.unihub.exception.ValidationException;
import tech.buildrun.unihub.repository.RoleRepository;
import tech.buildrun.unihub.repository.UserRepository;

import java.util.Collections;
import java.util.HashSet;

/**
 * Serviço responsável por operações de autenticação e registro de usuários.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registra um novo usuário no sistema.
     * Atribui a role padrão ROLE_USER.
     *
     * @param request DTO com username e password.
     * @return AuthResponse contendo o token JWT para o novo usuário.
     * @throws ValidationException se o username ou email já existirem.
     */
    @Transactional
    public AuthResponse register(AuthRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Nome de usuário já está em uso.");
        }
        // Em um sistema real, você também verificaria o email se ele fosse obrigatório no registro.

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // Atribui a role padrão ROLE_USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ValidationException("Role 'ROLE_USER' não encontrada. Contate o administrador."));
        newUser.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        userRepository.save(newUser);

        // Autentica o usuário recém-registrado e gera um token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateToken(authentication);

        return new AuthResponse(jwt);
    }

    /**
     * Autentica um usuário existente.
     *
     * @param request DTO com username e password.
     * @return AuthResponse contendo o token JWT para o usuário autenticado.
     * @throws BadCredentialsException se as credenciais forem inválidas.
     */
    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtService.generateToken(authentication);
            return new AuthResponse(jwt);
        } catch (BadCredentialsException ex) {
            throw new BadCredentialsException("Nome de usuário ou senha inválidos.");
        }
    }
}
