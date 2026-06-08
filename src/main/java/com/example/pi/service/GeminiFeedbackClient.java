package com.example.pi.service;

import com.example.pi.config.GeminiProperties;
import com.example.pi.dto.AiFeedbackRequest;
import com.example.pi.dto.AiFeedbackResponse;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiFeedbackClient {

    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final GeminiProperties properties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiFeedbackClient(GeminiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public AiFeedbackResponse suggestFeedback(AiFeedbackRequest request) {
        if (!properties.isConfigured()) {
            throw new IllegalStateException("Gemini API is not configured");
        }

        String prompt = buildPrompt(request);
        String url = BASE_URL + properties.getModel() + ":generateContent?key=" + properties.getApiKey();

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "temperature", 0.4,
                        "responseMimeType", "application/json"
                )
        );

        String responseBody = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(String.class);

        String jsonText = extractTextFromGeminiResponse(responseBody);
        return parseFeedbackResponse(jsonText, properties.getModel());
    }

    private String buildPrompt(AiFeedbackRequest request) {
        return """
                Tu es un assistant RH pour la plateforme B2U-HUB.
                Rédige un feedback professionnel en français pour une évaluation d'étudiant/freelance.

                Données :
                - Étudiant : %s
                - Projet : %s
                - Note globale : %d/5
                - Qualité des livrables : %d/5
                - Communication : %d/5
                - Professionnalisme : %d/5

                Réponds UNIQUEMENT avec un JSON valide (sans markdown) au format :
                {
                  "suggestedComment": "texte du feedback en 3-5 phrases",
                  "strengths": ["point fort 1", "point fort 2"],
                  "improvements": ["axe d'amélioration 1"],
                  "insightSummary": "résumé analytique court",
                  "confidenceScore": 0.85,
                  "modelLabel": "Gemini 2.0 Flash"
                }
                """.formatted(
                request.getStudentName(),
                request.getProjectTitle(),
                request.getRating(),
                orDefault(request.getQuality(), request.getRating()),
                orDefault(request.getCommunication(), request.getRating()),
                orDefault(request.getProfessionalism(), request.getRating())
        );
    }

    private int orDefault(Integer value, Integer fallback) {
        return value == null || value < 1 ? fallback : value;
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

    private AiFeedbackResponse parseFeedbackResponse(String jsonText, String model) {
        try {
            String cleaned = jsonText
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JsonNode node = objectMapper.readTree(cleaned);

            List<String> strengths = readStringList(node.path("strengths"));
            List<String> improvements = readStringList(node.path("improvements"));

            return AiFeedbackResponse.builder()
                    .suggestedComment(node.path("suggestedComment").asText())
                    .strengths(strengths)
                    .improvements(improvements)
                    .insightSummary(node.path("insightSummary").asText())
                    .confidenceScore(node.path("confidenceScore").asDouble(0.85))
                    .modelLabel(node.path("modelLabel").asText("Gemini " + model))
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse Gemini JSON feedback", e);
        }
    }

    private List<String> readStringList(JsonNode arrayNode) {
        List<String> values = new ArrayList<>();
        if (arrayNode.isArray()) {
            arrayNode.forEach(item -> values.add(item.asText()));
        }
        return values;
    }
}
