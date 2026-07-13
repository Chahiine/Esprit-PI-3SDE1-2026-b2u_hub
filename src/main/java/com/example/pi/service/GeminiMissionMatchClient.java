package com.example.pi.service;

import com.example.pi.config.GeminiProperties;
import com.example.pi.dto.AiMissionMatchResponse;
import com.example.pi.dto.MissionMatchItemDto;
import com.example.pi.entity.Mission;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GeminiMissionMatchClient {

    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final GeminiProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiMissionMatchClient(GeminiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public AiMissionMatchResponse matchMissions(
            String studentName,
            String skills,
            String preferredLocation,
            int maxResults,
            List<Mission> missions) {

        if (!properties.isConfigured()) {
            throw new IllegalStateException("Gemini API is not configured");
        }

        String missionsContext = missions.stream()
                .map(m -> "- id=%d | %s @ %s | lieu=%s | compétences=%s | %s".formatted(
                        m.getId(),
                        m.getTitle(),
                        m.getCompanyName(),
                        nullToDash(m.getLocation()),
                        nullToDash(m.getSkillsRequired()),
                        truncate(m.getDescription(), 120)))
                .collect(Collectors.joining("\n"));

        String prompt = """
                Tu es un moteur de matching IA pour B2U-HUB (plateforme missions étudiants/freelances).
                Analyse le profil étudiant et classe les missions ouvertes par pertinence.

                Profil étudiant :
                - Nom : %s
                - Compétences : %s
                - Localisation préférée : %s

                Missions ouvertes :
                %s

                Retourne UNIQUEMENT un JSON valide (sans markdown) avec max %d missions, format :
                {
                  "studentSummary": "résumé court du profil analysé",
                  "globalInsight": "synthèse globale du matching",
                  "confidenceScore": 0.88,
                  "matches": [
                    {
                      "missionId": 1,
                      "title": "titre",
                      "companyName": "entreprise",
                      "location": "ville",
                      "skillsRequired": "skills",
                      "matchScore": 85,
                      "matchLevel": "EXCELLENT",
                      "strengths": ["force 1"],
                      "gaps": ["lacune 1"],
                      "recommendation": "conseil court"
                    }
                  ]
                }

                matchLevel : EXCELLENT (>=75), BON (50-74), PARTIEL (<50).
                """.formatted(
                studentName,
                skills,
                nullToDash(preferredLocation),
                missionsContext,
                maxResults);

        String externalApiUrl = GEMINI_API_BASE + properties.getModel() + ":generateContent";
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "temperature", 0.3,
                        "responseMimeType", "application/json"
                )
        );

        String responseBody = restClient.post()
                .uri(externalApiUrl + "?key=" + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        return parseMatchResponse(extractText(responseBody), externalApiUrl, properties.getModel());
    }

    private AiMissionMatchResponse parseMatchResponse(String jsonText, String apiUrl, String model) {
        try {
            String cleaned = jsonText.replace("```json", "").replace("```", "").trim();
            JsonNode root = objectMapper.readTree(cleaned);

            List<MissionMatchItemDto> matches = new ArrayList<>();
            JsonNode matchesNode = root.path("matches");
            if (matchesNode.isArray()) {
                matchesNode.forEach(node -> matches.add(MissionMatchItemDto.builder()
                        .missionId(node.path("missionId").asLong())
                        .title(node.path("title").asText())
                        .companyName(node.path("companyName").asText())
                        .location(node.path("location").asText())
                        .skillsRequired(node.path("skillsRequired").asText())
                        .matchScore(node.path("matchScore").asInt(50))
                        .matchLevel(node.path("matchLevel").asText("BON"))
                        .strengths(readList(node.path("strengths")))
                        .gaps(readList(node.path("gaps")))
                        .recommendation(node.path("recommendation").asText())
                        .build()));
            }

            return AiMissionMatchResponse.builder()
                    .studentSummary(root.path("studentSummary").asText())
                    .matches(matches)
                    .globalInsight(root.path("globalInsight").asText())
                    .confidenceScore(root.path("confidenceScore").asDouble(0.85))
                    .externalApiUrl(apiUrl)
                    .modelLabel("Gemini " + model + " — matching sémantique (API externe)")
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Gemini mission match JSON", e);
        }
    }

    private List<String> readList(JsonNode arrayNode) {
        List<String> values = new ArrayList<>();
        if (arrayNode.isArray()) {
            arrayNode.forEach(item -> values.add(item.asText()));
        }
        return values;
    }

    private String extractText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                throw new IllegalStateException("Gemini returned empty match response");
            }
            return textNode.asText().trim();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read Gemini response", e);
        }
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String truncate(String text, int max) {
        if (text == null || text.isBlank()) return "—";
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }
}
