package com.unihub.app.service;

import com.unihub.app.dto.LoginRequest;
import com.unihub.app.dto.RegisterRequest;
import com.unihub.app.dto.UpdateUserRoleRequest;
import com.unihub.app.dto.UserDto;
import com.unihub.app.entity.ERole;
import com.unihub.app.entity.User;
import com.unihub.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private UserService userService;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, authenticationManager, jwtService);
    }

    @Test
    void save_registersUser_andReturnsDto_withEncodedPassword() {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setEmail("john@example.com");
        req.setPassword("secret123");
        req.setRole(ERole.ROLE_USER);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(inv -> {
            User u = userCaptor.getValue();
            u.setId(1L);
            return u;
        });

        UserDto dto = userService.save(req);
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("john", dto.getUsername());
        assertEquals("john@example.com", dto.getEmail());
        assertEquals(ERole.ROLE_USER, dto.getRole());

        User saved = userCaptor.getValue();
        assertNotEquals("secret123", saved.getPassword());
        assertTrue(encoder.matches("secret123", saved.getPassword()));
    }

    @Test
    void verify_authenticatesAndReturnsJwt_withClaims() {
        LoginRequest req = new LoginRequest();
        req.setUsername("john@example.com");
        req.setPassword("secret123");

        User user = new User();
        user.setId(10L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword(encoder.encode("secret123"));
        user.setRole(ERole.ROLE_ADMIN);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenReturn(new TestingAuthenticationToken("john@example.com", "secret123", "ROLE_ADMIN"));

        when(jwtService.generateToken(anyMap(), eq("john@example.com"))).thenReturn("jwt-token");

        String token = userService.verify(req);
        assertEquals("jwt-token", token);

        verify(jwtService, times(1)).generateToken(argThat(map ->
                map.get("userId").equals(10L) &&
                map.get("role").equals(ERole.ROLE_ADMIN) &&
                map.get("email").equals("john")
        ), eq("john@example.com"));
    }

    @Test
    void verify_whenUserNotFound_returnsMessage() {
        LoginRequest req = new LoginRequest();
        req.setUsername("missing@example.com");
        req.setPassword("pwd");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        String result = userService.verify(req);
        assertEquals("Usuário não encontrado.", result);

        verify(authenticationManager, never()).authenticate(any());
        verify(jwtService, never()).generateToken(anyMap(), anyString());
    }

    @Test
    void verify_whenAuthenticationNotAuthenticated_returnsFailed() {
        LoginRequest req = new LoginRequest();
        req.setUsername("john@example.com");
        req.setPassword("wrong");

        User user = new User();
        user.setId(10L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword(encoder.encode("secret123"));
        user.setRole(ERole.ROLE_USER);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        TestingAuthenticationToken unauth = new TestingAuthenticationToken("john@example.com", "wrong");
        unauth.setAuthenticated(false);
        when(authenticationManager.authenticate(any())).thenReturn(unauth);

        String result = userService.verify(req);
        assertEquals("Failed", result);
        verify(jwtService, never()).generateToken(anyMap(), anyString());
    }

    @Test
    void getAllUsers_returnsMappedDtos() {
        User u1 = new User();
        u1.setId(1L);
        u1.setUsername("a");
        u1.setEmail("a@x");
        u1.setRole(ERole.ROLE_USER);
        User u2 = new User();
        u2.setId(2L);
        u2.setUsername("b");
        u2.setEmail("b@x");
        u2.setRole(ERole.ROLE_ADMIN);

        when(userRepository.findAll()).thenReturn(List.of(u1, u2));
        List<UserDto> dtos = userService.getAllUsers();
        assertEquals(2, dtos.size());
        assertEquals(1L, dtos.get(0).getId());
        assertEquals(ERole.ROLE_USER, dtos.get(0).getRole());
        assertEquals(2L, dtos.get(1).getId());
        assertEquals(ERole.ROLE_ADMIN, dtos.get(1).getRole());
    }

    @Test
    void getUserById_returnsDto() {
        User u = new User();
        u.setId(3L);
        u.setUsername("c");
        u.setEmail("c@x");
        u.setRole(ERole.ROLE_USER);

        when(userRepository.findById(3L)).thenReturn(Optional.of(u));
        UserDto dto = userService.getUserById(3L);
        assertEquals(3L, dto.getId());
        assertEquals("c", dto.getUsername());
        assertEquals("c@x", dto.getEmail());
        assertEquals(ERole.ROLE_USER, dto.getRole());
    }

    @Test
    void updateUserRole_updatesAndSaves() {
        User u = new User();
        u.setId(4L);
        u.setUsername("d");
        u.setEmail("d@x");
        u.setRole(ERole.ROLE_USER);

        when(userRepository.findById(4L)).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateUserRoleRequest req = new UpdateUserRoleRequest();
        req.setRole(ERole.ROLE_ADMIN);

        UserDto dto = userService.updateUserRole(4L, req);
        assertEquals(ERole.ROLE_ADMIN, dto.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
