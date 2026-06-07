package com.example.pi.controller;

import com.example.pi.dto.*;
import com.example.pi.entity.Role;
import com.example.pi.entity.User;
import com.example.pi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try { return ResponseEntity.ok(userService.register(req)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try { return ResponseEntity.ok(userService.login(req)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        return ResponseEntity.ok(userService.getProfile(user));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateMe(@AuthenticationPrincipal User user, @RequestBody UpdateProfileRequest req) {
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        try { return ResponseEntity.ok(userService.updateProfile(user, req)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal User user, @RequestBody ChangePasswordRequest req) {
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        try { userService.changePassword(user, req); return ResponseEntity.ok("Password updated"); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/me/profile-image")
    public ResponseEntity<?> uploadProfileImage(@AuthenticationPrincipal User user, @RequestParam("file") MultipartFile file) {
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        try { return ResponseEntity.ok(userService.uploadProfileImage(user, file)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/me/cv")
    public ResponseEntity<?> uploadCv(@AuthenticationPrincipal User user, @RequestParam("file") MultipartFile file) {
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");
        try { return ResponseEntity.ok(userService.uploadCv(user, file)); }
        catch (RuntimeException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() { return userService.getAllUsers(); }

    @GetMapping("/admin/by-role")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getByRole(@RequestParam Role role) { return userService.getUsersByRole(role); }

    @PutMapping("/admin/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> ban(@PathVariable Long id) {
        try { return ResponseEntity.ok(userService.setUserActive(id, false)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PutMapping("/admin/{id}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> unban(@PathVariable Long id) {
        try { return ResponseEntity.ok(userService.setUserActive(id, true)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try { userService.deleteUser(id); return ResponseEntity.noContent().build(); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> stats() { return ResponseEntity.ok(userService.getStats()); }
}
