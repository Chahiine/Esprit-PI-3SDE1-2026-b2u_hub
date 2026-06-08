package com.example.pi.controller;

import com.example.pi.entity.Role;
import com.example.pi.entity.User;
import com.example.pi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void register_returnsCreatedUser() throws Exception {
        User user = sampleUser();
        user.setId(1L);
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUser())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("student@test.com"));
    }

    @Test
    void register_returnsBadRequestOnError() throws Exception {
        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUser())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers_returnsList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Samira"));
    }

    @Test
    void getUser_returnsNotFound() throws Exception {
        when(userService.getUserById(99L))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateUser_returnsBadRequestOnError() throws Exception {
        doThrow(new IllegalArgumentException("Invalid"))
                .when(userService).updateUser(eq(1L), any(User.class));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleUser())))
                .andExpect(status().isBadRequest());
    }

    private User sampleUser() {
        return User.builder()
                .firstName("Samira")
                .lastName("Benali")
                .email("student@test.com")
                .password("password123")
                .role(Role.STUDENT)
                .build();
    }
}
