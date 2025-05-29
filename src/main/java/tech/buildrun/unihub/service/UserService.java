package tech.buildrun.unihub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.buildrun.unihub.dto.UserRequest;
import tech.buildrun.unihub.dto.UserResponse;
import tech.buildrun.unihub.entity.Role;
import tech.buildrun.unihub.entity.User;
import tech.buildrun.unihub.exception.ResourceNotFoundException;
import tech.buildrun.unihub.exception.ValidationException;
import tech.buildrun.unihub.repository.RoleRepository;
import tech.buildrun.unihub.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento de usuários.
 * Operações de CRUD e atualização de roles (apenas para ADMIN).
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Converte uma entidade User para UserResponse DTO.
     */
    private UserResponse toUserResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), roleNames, user.getCreatedAt(), user.getUpdatedAt());
    }

    /**
     * Obtém todos os usuários. Requer role ADMIN.
     * @return Lista de UserResponse.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtém um usuário por ID. Requer role ADMIN.
     * @param id ID do usuário.
     * @return UserResponse.
     * @throws ResourceNotFoundException se o usuário não for encontrado.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));
        return toUserResponse(user);
    }

    /**
     * Cria um novo usuário. Requer role ADMIN.
     * @param request DTO com dados do usuário.
     * @return UserResponse do usuário criado.
     * @throws ValidationException se username ou email já existirem.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse createUser(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Nome de usuário já está em uso.");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("E-mail já está em uso.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());

        // Atribui roles com base na requisição
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ValidationException("Role '" + roleName + "' não encontrada."));
                roles.add(role);
            }
            user.setRoles(roles);
        } else {
            // Se nenhuma role for especificada, atribui ROLE_USER por padrão
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ValidationException("Role 'ROLE_USER' não encontrada."));
            user.setRoles(new HashSet<>(List.of(userRole)));
        }

        User savedUser = userRepository.save(user);
        return toUserResponse(savedUser);
    }

    /**
     * Atualiza um usuário existente. Requer role ADMIN.
     * @param id ID do usuário a ser atualizado.
     * @param request DTO com dados atualizados.
     * @return UserResponse do usuário atualizado.
     * @throws ResourceNotFoundException se o usuário não for encontrado.
     * @throws ValidationException se username ou email já existirem por outro usuário.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse updateUser(UUID id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Nome de usuário já está em uso por outro usuário.");
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("E-mail já está em uso por outro usuário.");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Atualiza roles
        if (request.getRoles() != null) {
            Set<Role> newRoles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ValidationException("Role '" + roleName + "' não encontrada."));
                newRoles.add(role);
            }
            user.setRoles(newRoles);
        }

        User updatedUser = userRepository.save(user);
        return toUserResponse(updatedUser);
    }

    /**
     * Deleta um usuário. Requer role ADMIN.
     * @param id ID do usuário a ser deletado.
     * @throws ResourceNotFoundException se o usuário não for encontrado.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário", "id", id);
        }
        userRepository.deleteById(id);
    }
}
