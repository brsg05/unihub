package com.unihub.app.service;

import com.unihub.app.dto.*;
import com.unihub.app.entity.User;
import com.unihub.app.exception.ResourceNotFoundException;
import com.unihub.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;


    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return convertToDto(user);
    }

    @Transactional
    public UserDto updateUserRole(Long userId, UpdateUserRoleRequest roleRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setRole(roleRequest.getRole());
        User updatedUser = userRepository.save(user);
        return convertToDto(updatedUser);
    }

    private UserDto convertToDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }

    private User convertToEntity(RegisterRequest requestDTO) {
        User user = new User();
        user.setUsername(requestDTO.getUsername());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(requestDTO.getPassword());
        System.out.println("Convert to entity ROLE ATUAL: " + requestDTO.getRole());// Placeholder, será hashado no serviço
        user.setRole(requestDTO.getRole());
        // created_at e updated_at são automáticos via @PrePersist
        return user;
    }

    @Transactional
    public UserDto save(RegisterRequest requestDTO) {
        // Validações de negócio adicionais antes de salvar
        if (userRepository.findByEmail(requestDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }

        User user = convertToEntity(requestDTO);

        // HASHING DA SENHA: Crucial para segurança!
        // No lugar de 'requestDTO.getPassword()', você usaria:
        // user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        // Por enquanto, apenas um placeholder para compilação:
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword())); // Apenas para testes, SUBSTITUA por um HASH REAL!

        try {
            User savedUser = userRepository.save(user);

            return convertToDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            // Captura violações de unicidade ou outras constraints que a validação DTO não pegou
            throw new IllegalArgumentException("Erro ao cadastrar usuário: e-mail ou CPF já existem, ou dados inválidos.", e);
        }
    }


    public String verify(LoginRequest requestDTO) {
        Optional<User> userOpt = userRepository.findByEmail(requestDTO.getUsername());
        if (userOpt.isEmpty()) {
            return "Usuário não encontrado.";
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                requestDTO.getUsername(), requestDTO.getPassword()));

        if(authentication.isAuthenticated()) {

            Optional<User> user = userRepository.findByEmail(requestDTO.getUsername());

            Map<String, Object> claims = null;
            if (user.isPresent()) {
                claims = new HashMap<>();
                claims.put("userId", user.get().getId());
                claims.put("role", user.get().getRole());
                claims.put("email", user.get().getUsername());
            }

            return jwtService.generateToken(claims, requestDTO.getUsername());
        }
        return "Failed";
    }
} 