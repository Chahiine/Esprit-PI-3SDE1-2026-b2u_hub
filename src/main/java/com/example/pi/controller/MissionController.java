package com.example.pi.controller;

import com.example.pi.dto.MissionDto;
import com.example.pi.entity.Mission;
import com.example.pi.service.MissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    // ── PUBLIC: liste des missions ouvertes ───────────────────
    @GetMapping("/open")
    public List<Mission> getOpen() {
        return missionService.getOpen();
    }

    // ── PUBLIC: détail d'une mission ──────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try { return ResponseEntity.ok(missionService.getById(id)); }
        catch (IllegalArgumentException e) { return ResponseEntity.notFound().build(); }
    }

    // ── PUBLIC: recherche ─────────────────────────────────────
    @GetMapping("/search")
    public List<Mission> search(@RequestParam(defaultValue = "") String keyword) {
        return missionService.search(keyword);
    }

    // ── AUTHENTICATED: toutes les missions (admin) ────────────
    @GetMapping
    public List<Mission> getAll() {
        return missionService.getAll();
    }

    // ── AUTHENTICATED: mes missions (par user) ────────────────
    @GetMapping("/my/{userId}")
    public List<Mission> getMyMissions(@PathVariable Long userId) {
        return missionService.getByUser(userId);
    }

    // ── AUTHENTICATED: créer une mission ─────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody MissionDto dto) {
        try { return ResponseEntity.ok(missionService.create(dto)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // ── AUTHENTICATED: modifier une mission ──────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody MissionDto dto) {
        try { return ResponseEntity.ok(missionService.update(id, dto)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // ── AUTHENTICATED: supprimer une mission ─────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try { missionService.delete(id); return ResponseEntity.noContent().build(); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // ── STATS ─────────────────────────────────────────────────
    @GetMapping("/stats")
    public ResponseEntity<?> stats() {
        return ResponseEntity.ok(missionService.getStats());
    }
}
