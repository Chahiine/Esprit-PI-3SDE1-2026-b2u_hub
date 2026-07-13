package com.example.pi.service;

import com.example.pi.config.GeminiProperties;
import com.example.pi.dto.AiMissionDescriptionRequest;
import com.example.pi.dto.AiMissionDescriptionResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Client HTTP vers l'API externe Google Gemini pour générer des descriptions de mission.
 */
@Service
public class GeminiMissionClient {

    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final GeminiProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiMissionClient(GeminiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public AiMissionDescriptionResponse suggestDescription(AiMissionDescriptionRequest request) {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("Gemini API is not configured");
        }

        String prompt = buildPrompt(request);
        String externalApiUrl = GEMINI_API_BASE + properties.getModel() + ":generateContent";

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.5,
                        "responseMimeType", "application/json"
                )
        );

        String responseBody = restClient.post()
                .uri(externalApiUrl + "?key=" + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        String jsonText = extractTextFromGeminiResponse(responseBody);
        return parseMissionResponse(jsonText, externalApiUrl, properties.getModel());
    }

    private String buildPrompt(AiMissionDescriptionRequest request) {
        return """
                Tu es un assistant RH pour la plateforme B2U-HUB (missions étudiantes / freelances).
                Rédige une offre de mission professionnelle en français.

                Données :
                - Titre : %s
                - Entreprise : %s
                - Localisation : %s
                - Compétences requises : %s
                - Durée : %s
                - Lien externe (optionnel) : %s

                Réponds UNIQUEMENT avec un JSON valide (sans markdown) au format :
                {
                  "suggestedTitle": "titre optimisé",
                  "suggestedDescription": "description complète en 4-6 phrases",
                  "insightSummary": "résumé court pour l'entreprise",
                  "confidenceScore": 0.88
                }
                """.formatted(
                nullToDash(request.getTitle()),
                nullToDash(request.getCompanyName()),
                nullToDash(request.getLocation()),
                nullToDash(request.getSkillsRequired()),
                nullToDash(request.getDuration()),
                nullToDash(request.getExternalLink())
        );
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String extractTextFromGeminiResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                JsonNode error = root.path("error").path("message");
                if (!error.isMissingNode()) {
                    throw new IllegalStateException("Gemini error: " + error.asText());
                }
                throw new IllegalStateException("Gemini returned an empty response");
            }
            return textNode.asText().trim();
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to read Gemini response", e);
        }
    }

    private AiMissionDescriptionResponse parseMissionResponse(String jsonText, String apiUrl, String model) {
        try {
            String cleaned = jsonText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);

            return AiMissionDescriptionResponse.builder()
                    .suggestedTitle(node.path("suggestedTitle").asText())
                    .suggestedDescription(node.path("suggestedDescription").asText())
                    .insightSummary(node.path("insightSummary").asText())
                    .confidenceScore(node.path("confidenceScore").asDouble(0.85))
                    .externalApiUrl(apiUrl)
                    .modelLabel("Gemini " + model + " (API externe)")
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Gemini JSON mission description", e);
        }
    }
}
