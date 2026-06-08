package com.example.pi.controller;

import com.example.pi.entity.Role;
import com.example.pi.entity.User;
import com.example.pi.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @Test
    void register_returnsCreatedUser() {
        User user = sampleUser();
        user.setId(1L);
        when(userService.createUser(any(User.class))).thenReturn(user);

        ResponseEntity<?> result = controller.register(sampleUser());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(User.class);
    }

    @Test
    void register_returnsBadRequestOnError() {
        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        ResponseEntity<?> result = controller.register(sampleUser());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getUsers_returnsList() {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser()));

        List<User> result = controller.getUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("Samira");
    }

    @Test
    void getUser_returnsNotFound() {
        when(userService.getUserById(99L))
                .thenThrow(new IllegalArgumentException("User not found"));

        ResponseEntity<?> result = controller.getUser(99L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteUser_returnsNoContent() {
        ResponseEntity<Void> result = controller.deleteUser(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void updateUser_returnsBadRequestOnError() {
        doThrow(new IllegalArgumentException("Invalid"))
                .when(userService).updateUser(eq(1L), any(User.class));

        ResponseEntity<?> result = controller.updateUser(1L, sampleUser());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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
