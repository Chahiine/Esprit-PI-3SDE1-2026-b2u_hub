package com.example.pi.controller;

import com.example.pi.dto.AiMissionDescriptionRequest;
import com.example.pi.dto.AiMissionMatchRequest;
import com.example.pi.service.AiMissionMatchService;
import com.example.pi.service.AiMissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("*")
public class AiMissionController {

    private final AiMissionService aiMissionService;
    private final AiMissionMatchService aiMissionMatchService;

    public AiMissionController(
            AiMissionService aiMissionService,
            AiMissionMatchService aiMissionMatchService) {
        this.aiMissionService = aiMissionService;
        this.aiMissionMatchService = aiMissionMatchService;
    }

    /**
     * IA avancée : génère une description de mission via l'API externe Gemini
     * (ou moteur local si l'API n'est pas configurée).
     */
    @PostMapping("/suggest-mission-description")
    public ResponseEntity<?> suggestMissionDescription(@RequestBody AiMissionDescriptionRequest request) {
        try {
            return ResponseEntity.ok(aiMissionService.suggestDescription(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * IA avancée (différente du feedback texte) : matching intelligent profil étudiant ↔ missions ouvertes.
     * Retourne scores, forces, lacunes et recommandations via API externe Gemini.
     */
    @PostMapping("/match-missions")
    public ResponseEntity<?> matchMissions(@RequestBody AiMissionMatchRequest request) {
        try {
            return ResponseEntity.ok(aiMissionMatchService.matchMissions(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
