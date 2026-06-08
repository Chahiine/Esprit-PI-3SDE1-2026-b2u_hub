package com.example.pi.service;

import com.example.pi.config.GeminiProperties;
import com.example.pi.dto.AiFeedbackRequest;
import com.example.pi.dto.AiFeedbackResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Assistant IA pour rédiger des feedbacks d'évaluation.
 * Utilise Gemini si configuré, sinon le moteur local (règles métier).
 */
@Service
public class AiFeedbackService {

    private static final Logger log = LoggerFactory.getLogger(AiFeedbackService.class);

    private final GeminiProperties geminiProperties;
    private final GeminiFeedbackClient geminiFeedbackClient;

    public AiFeedbackService(GeminiProperties geminiProperties, GeminiFeedbackClient geminiFeedbackClient) {
        this.geminiProperties = geminiProperties;
        this.geminiFeedbackClient = geminiFeedbackClient;
    }

    public AiFeedbackResponse suggestFeedback(AiFeedbackRequest request) {
        validate(request);

        if (geminiProperties.isConfigured()) {
            try {
                return geminiFeedbackClient.suggestFeedback(request);
            } catch (Exception e) {
                log.warn("Gemini unavailable, using local fallback: {}", e.getMessage());
            }
        }

        return suggestFeedbackLocally(request);
    }

    private AiFeedbackResponse suggestFeedbackLocally(AiFeedbackRequest request) {
        int rating = clamp(request.getRating());
        int quality = clamp(orDefault(request.getQuality(), rating));
        int communication = clamp(orDefault(request.getCommunication(), rating));
        int professionalism = clamp(orDefault(request.getProfessionalism(), rating));
        double average = (quality + communication + professionalism) / 3.0;

        List<String> strengths = new ArrayList<>();
        List<String> improvements = new ArrayList<>();

        if (quality >= 4) {
            strengths.add("Livrables de bonne qualité, conformes aux attentes du projet.");
        } else if (quality <= 2) {
            improvements.add("Renforcer la qualité et la finition des livrables.");
        }

        if (communication >= 4) {
            strengths.add("Communication claire et réactive avec l'équipe.");
        } else if (communication <= 2) {
            improvements.add("Améliorer la réactivité et la clarté des échanges.");
        }

        if (professionalism >= 4) {
            strengths.add("Attitude professionnelle et respect des délais.");
        } else if (professionalism <= 2) {
            improvements.add("Travailler la ponctualité et le professionnalisme au quotidien.");
        }

        if (strengths.isEmpty() && average >= 3.5) {
            strengths.add("Participation correcte au projet " + request.getProjectTitle() + ".");
        }
        if (improvements.isEmpty() && average < 3.5) {
            improvements.add("Consolider les compétences techniques et relationnelles sur de futurs projets.");
        }

        String tone = average >= 4 ? "excellent" : average >= 3 ? "satisfaisant" : "à renforcer";
        String comment = buildComment(request.getStudentName(), request.getProjectTitle(), rating, tone, strengths, improvements);

        String insight = String.format(
                "Profil analysé : note globale %d/5, moyenne critères %.1f/5 — recommandation %s pour le scoring B2U-HUB.",
                rating, average, average >= 4 ? "prioritaire" : average >= 3 ? "standard" : "accompagnement");

        double confidence = 0.75 + (average / 5.0) * 0.2;

        return AiFeedbackResponse.builder()
                .suggestedComment(comment)
                .strengths(strengths)
                .improvements(improvements)
                .insightSummary(insight)
                .confidenceScore(Math.round(confidence * 100.0) / 100.0)
                .modelLabel("B2U-HUB AI Assistant (analyse multi-critères)")
                .build();
    }

    private String buildComment(String student, String project, int rating, String tone,
                                List<String> strengths, List<String> improvements) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(
                "%s a mené le projet « %s » avec un niveau %s (note globale : %d/5). ",
                student, project, tone, rating));

        if (!strengths.isEmpty()) {
            sb.append("Points forts : ");
            sb.append(String.join(" ", strengths));
        }
        if (!improvements.isEmpty()) {
            if (!strengths.isEmpty()) {
                sb.append(" ");
            }
            sb.append("Axes d'amélioration : ");
            sb.append(String.join(" ", improvements));
        }
        sb.append(" Ce retour a été assisté par l'IA B2U-HUB à partir des critères saisis.");
        return sb.toString();
    }

    private void validate(AiFeedbackRequest request) {
        if (!StringUtils.hasText(request.getStudentName())) {
            throw new IllegalArgumentException("studentName is required for AI suggestion");
        }
        if (!StringUtils.hasText(request.getProjectTitle())) {
            throw new IllegalArgumentException("projectTitle is required for AI suggestion");
        }
        if (request.getRating() == null) {
            throw new IllegalArgumentException("rating is required for AI suggestion");
        }
    }

    private int clamp(Integer value) {
        if (value == null) return 1;
        return Math.max(1, Math.min(5, value));
    }

    private int orDefault(Integer value, int fallback) {
        return value == null || value < 1 ? fallback : value;
    }
}
