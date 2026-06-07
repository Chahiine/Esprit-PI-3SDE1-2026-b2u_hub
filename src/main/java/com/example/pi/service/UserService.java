package com.example.pi.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.pi.dto.*;
import com.example.pi.entity.Role;
import com.example.pi.entity.User;
import com.example.pi.repository.UserRepository;
import com.example.pi.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Cloudinary cloudinary;

    public AuthResponse register(RegisterRequest req) {
        if (!StringUtils.hasText(req.getFirstName())) throw new IllegalArgumentException("First name is required");
        if (!StringUtils.hasText(req.getLastName()))  throw new IllegalArgumentException("Last name is required");
        if (!StringUtils.hasText(req.getEmail()) || !req.getEmail().contains("@")) throw new IllegalArgumentException("Valid email is required");
        if (!StringUtils.hasText(req.getPassword()) || req.getPassword().length() < 6) throw new IllegalArgumentException("Password must be at least 6 characters");
        if (userRepository.existsByEmail(req.getEmail())) throw new IllegalArgumentException("Email already in use");

        User user = User.builder()
                .firstName(req.getFirstName())
                .lastName(req.getLastName())
                .email(req.getEmail().toLowerCase())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(req.getRole() != null ? req.getRole() : Role.STUDENT)
                .active(true).build();

        user = userRepository.save(user);
        return toAuthResponse(user, jwtService.generateToken(user.getEmail()));
    }

    public AuthResponse login(LoginRequest req) {
        if (!StringUtils.hasText(req.getEmail()) || !StringUtils.hasText(req.getPassword()))
            throw new IllegalArgumentException("Email and password are required");

        User user = userRepository.findByEmail(req.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!user.isActive()) throw new IllegalArgumentException("Account is disabled");
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            throw new IllegalArgumentException("Invalid email or password");

        return toAuthResponse(user, jwtService.generateToken(user.getEmail()));
    }

    public AuthResponse getProfile(User user) { return toAuthResponse(user, null); }

    public AuthResponse updateProfile(User user, UpdateProfileRequest req) {
        if (StringUtils.hasText(req.getFirstName())) user.setFirstName(req.getFirstName());
        if (StringUtils.hasText(req.getLastName()))  user.setLastName(req.getLastName());
        if (req.getBio()    != null) user.setBio(req.getBio());
        if (req.getSkills() != null) user.setSkills(req.getSkills());
        return toAuthResponse(userRepository.save(user), null);
    }

    public void changePassword(User user, ChangePasswordRequest req) {
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword()))
            throw new IllegalArgumentException("Current password is incorrect");
        if (!StringUtils.hasText(req.getNewPassword()) || req.getNewPassword().length() < 6)
            throw new IllegalArgumentException("New password must be at least 6 characters");
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    public String uploadProfileImage(User user, MultipartFile file) {
        try {
            Map result = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("folder","b2u_hub/profiles","public_id","user_"+user.getId(),"overwrite",true));
            String url = (String) result.get("secure_url");
            user.setProfileImageUrl(url);
            userRepository.save(user);
            return url;
        } catch (Exception e) { throw new RuntimeException("Image upload failed: " + e.getMessage()); }
    }

    public String uploadCv(User user, MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            Map result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "resource_type", "auto",       // ← auto works for PDF viewing
                            "public_id",     originalFilename,
                            "use_filename",  true,
                            "unique_filename", true,
                            "overwrite",     false,
                            "folder",        "cv_files",
                            "access_mode",   "public"
                    )
            );
            String url = result.get("secure_url").toString();
            user.setCvUrl(url);
            userRepository.save(user);
            return url;
        } catch (Exception e) {
            throw new RuntimeException("CV upload failed: " + e.getMessage());
        }
    }

    public List<User> getAllUsers() { return userRepository.findAll(); }
    public List<User> getUsersByRole(Role role) { return userRepository.findByRole(role); }

    public User setUserActive(Long id, boolean active) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(active);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) throw new IllegalArgumentException("User not found");
        userRepository.deleteById(id);
    }

    public Map<String, Long> getStats() {
        return Map.of(
            "total", userRepository.count(),
            "students", userRepository.countByRole(Role.STUDENT),
            "companies", userRepository.countByRole(Role.COMPANY),
            "admins", userRepository.countByRole(Role.ADMIN)
        );
    }

    private AuthResponse toAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token).userId(user.getId())
                .firstName(user.getFirstName()).lastName(user.getLastName())
                .email(user.getEmail()).role(user.getRole())
                .profileImageUrl(user.getProfileImageUrl()).cvUrl(user.getCvUrl())
                .bio(user.getBio()).skills(user.getSkills()).build();
    }
}
