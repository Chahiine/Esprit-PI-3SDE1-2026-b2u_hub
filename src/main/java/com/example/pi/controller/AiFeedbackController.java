package com.example.pi.controller;

import com.example.pi.dto.AiFeedbackRequest;
import com.example.pi.dto.AiFeedbackResponse;
import com.example.pi.service.AiFeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("*")
public class AiFeedbackController {

    private final AiFeedbackService aiFeedbackService;

    public AiFeedbackController(AiFeedbackService aiFeedbackService) {
        this.aiFeedbackService = aiFeedbackService;
    }

    /**
     * Tâche avancée IA : génère un feedback professionnel à partir des notes et du contexte projet.
     */
    @PostMapping("/suggest-feedback")
    public ResponseEntity<?> suggestFeedback(@RequestBody AiFeedbackRequest request) {
        try {
            AiFeedbackResponse response = aiFeedbackService.suggestFeedback(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
