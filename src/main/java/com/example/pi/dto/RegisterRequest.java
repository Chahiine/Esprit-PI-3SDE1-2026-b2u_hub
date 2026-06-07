package com.example.pi.dto;
import com.example.pi.entity.Role;
import lombok.Data;
@Data
public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
}
