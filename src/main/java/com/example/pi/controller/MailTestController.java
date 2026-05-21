package com.example.pi.controller;

import com.example.pi.service.EmailNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin("*")
public class MailTestController {

    private final EmailNotificationService emailNotificationService;

    public MailTestController(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    /**
     * Test d'envoi e-mail sans créer d'évaluation.
     * POST body: { "to": "sassichahine68@gmail.com" }
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testMail(@RequestBody Map<String, String> body) {
        String to = body != null ? body.get("to") : null;
        if (to == null || to.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Champ \"to\" requis (ex: sassichahine68@gmail.com)"));
        }
        String result = emailNotificationService.sendTestEmail(to.trim());
        boolean ok = result.startsWith("E-mail de test envoye");
        return ResponseEntity.ok(Map.of(
                "success", ok,
                "message", result,
                "to", to.trim()));
    }
}
