package com.example.pi.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor  // si tu utilises lombok, sinon constructeur normal
public class AiMatchingController {

    @Value("${huggingface.api-key}")
    private String hfKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @Data
    public static class MatchingRequest {
        private String studentSkills;
        private String studentBio;
        private String missionTitle;
        private String missionDescription;
        private String missionSkillsRequired;
    }

    @PostMapping("/smart-matching")
    public ResponseEntity<?> smartMatching(@RequestBody MatchingRequest req) {
        try {
            String prompt = buildPrompt(req);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(hfKey);

            Map<String, Object> body = Map.of(
                    "inputs", prompt,
                    "parameters", Map.of(
                            "max_new_tokens", 500,
                            "temperature", 0.2,
                            "return_full_text", false
                    )
            );

            String url = "https://api-inference.huggingface.co/models/mistralai/Mistral-7B-Instruct-v0.3";
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Object[]> response = restTemplate.postForEntity(url, entity, Object[].class);

            if (response.getBody() != null && response.getBody().length > 0) {
                @SuppressWarnings("unchecked")
                Map<String, Object> first = (Map<String, Object>) response.getBody()[0];
                String text = (String) first.get("generated_text");
                return ResponseEntity.ok(Map.of("text", text));
            }
            return ResponseEntity.badRequest().body("No response from AI");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("AI Error: " + e.getMessage());
        }
    }

    private String buildPrompt(MatchingRequest req) {
        return """
            <s>[INST] You are an expert recruiter. Analyze student-mission compatibility.
            Return ONLY valid JSON, no extra text:
            {
              "score": <0-100>,
              "level": "<Excellent|Bon|Moyen|Faible>",
              "summary": "<2 sentences in French>",
              "matchedSkills": ["skill1"],
              "missingSkills": ["skill1"],
              "recommendation": "<1 sentence in French>"
            }

            Student skills: %s
            Student bio: %s
            Mission: %s
            Description: %s
            Required skills: %s
            [/INST]
            """.formatted(
                req.getStudentSkills(),
                req.getStudentBio(),
                req.getMissionTitle(),
                req.getMissionDescription(),
                req.getMissionSkillsRequired()
        );
    }
}