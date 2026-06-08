package com.example.pi.service;

import com.example.pi.entity.Role;
import com.example.pi.entity.User;
import com.example.pi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .firstName("Chahine")
                .lastName("Sassi")
                .email("chahine@test.com")
                .password("secret123")
                .role(Role.STUDENT)
                .skills("Java, Angular")
                .build();
    }

    @Test
    void createUser_savesNewUser() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        User created = userService.createUser(user);

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getEmail()).isEqualTo("chahine@test.com");
    }

    @Test
    void createUser_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");
    }

    @Test
    void getAllUsers_returnsList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        assertThat(userService.getAllUsers()).hasSize(1);
    }

    @Test
    void getUserById_returnsUser() {
        user.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertThat(userService.getUserById(2L).getFirstName()).isEqualTo("Chahine");
    }

    @Test
    void getUserById_throwsWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUser_modifiesExistingUser() {
        User existing = User.builder()
                .id(1L)
                .firstName("Old")
                .lastName("Name")
                .email("old@test.com")
                .password("oldpass")
                .role(Role.COMPANY)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User updated = userService.updateUser(1L, user);

        assertThat(updated.getFirstName()).isEqualTo("Chahine");
        assertThat(updated.getRole()).isEqualTo(Role.STUDENT);
    }

    @Test
    void deleteUser_removesUser() {
        user.setId(3L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));

        userService.deleteUser(3L);

        verify(userRepository).delete(user);
    }

    @Test
    void createUser_rejectsShortPassword() {
        user.setPassword("123");

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Password must be at least 6 characters");
    }

    @Test
    void createUser_rejectsMissingRole() {
        user.setRole(null);

        assertThatThrownBy(() -> userService.createUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Role is required");
    }
}
