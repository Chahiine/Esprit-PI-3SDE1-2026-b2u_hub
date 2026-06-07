package com.example.pi.dto;
import com.example.pi.entity.Role;
import lombok.*;
@Data @Builder @AllArgsConstructor @NoArgsConstructor
public class AuthResponse {
    private String token;
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String profileImageUrl;
    private String cvUrl;
    private String bio;
    private String skills;
}
