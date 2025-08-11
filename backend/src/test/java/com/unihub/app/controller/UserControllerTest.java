package com.unihub.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unihub.app.dto.RegisterRequest;
import com.unihub.app.dto.UserDto;
import com.unihub.app.dto.LoginRequest;
import com.unihub.app.entity.ERole;
import com.unihub.app.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserService userService;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        userService = mock(UserService.class);
        UserController controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void login_returnsJwtString() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("john@example.com");
        req.setPassword("secret123");

        when(userService.verify(any(LoginRequest.class))).thenReturn("jwt-token");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));
    }

    @Test
    void register_returnsCreatedUser() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("john");
        req.setEmail("john@example.com");
        req.setPassword("secret123");
        req.setRole(ERole.ROLE_USER);

        UserDto dto = new UserDto(1L, "john", "john@example.com", ERole.ROLE_USER);
        when(userService.save(any(RegisterRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(dto)));
    }
}
