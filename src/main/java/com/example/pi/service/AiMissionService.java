package com.example.pi.service;

import com.example.pi.config.GeminiProperties;
import com.example.pi.dto.AiMissionDescriptionRequest;
import com.example.pi.dto.AiMissionDescriptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Génération IA de description de mission via API externe Gemini, avec repli local.
 */
@Service
public class AiMissionService {

    private static final Logger log = LoggerFactory.getLogger(AiMissionService.class);
    private static final String GEMINI_API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";

    private final GeminiProperties geminiProperties;
    private final GeminiMissionClient geminiMissionClient;

    public AiMissionService(GeminiProperties geminiProperties, GeminiMissionClient geminiMissionClient) {
        this.geminiProperties = geminiProperties;
        this.geminiMissionClient = geminiMissionClient;
    }

    public AiMissionDescriptionResponse suggestDescription(AiMissionDescriptionRequest request) {
        validate(request);

        if (geminiProperties.isConfigured()) {
            try {
                return geminiMissionClient.suggestDescription(request);
            } catch (Exception e) {
                log.warn("Gemini API externe indisponible, fallback local: {}", e.getMessage());
            }
        }

        return suggestDescriptionLocally(request);
    }

    private AiMissionDescriptionResponse suggestDescriptionLocally(AiMissionDescriptionRequest request) {
        String title = request.getTitle().trim();
        String company = hasText(request.getCompanyName()) ? request.getCompanyName().trim() : "notre entreprise";
        String location = hasText(request.getLocation()) ? request.getLocation().trim() : "Tunis";
        String skills = hasText(request.getSkillsRequired()) ? request.getSkillsRequired().trim() : "compétences techniques adaptées";
        String duration = hasText(request.getDuration()) ? request.getDuration().trim() : "3 mois";

        String description = String.format(
                "%s recrute pour la mission « %s » basée à %s pour une durée de %s. "
                        + "Le profil recherché maîtrise : %s. "
                        + "Vous participerez à un projet concret au sein de l'équipe, avec un encadrement régulier et des livrables définis. "
                        + "Cette offre est publiée sur B2U-HUB pour connecter étudiants et entreprises.",
                company, title, location, duration, skills
        );

        String insight = String.format(
                "Mission orientée %s — prioriser les candidats avec %s.",
                location, skills
        );

        String externalLinkNote = hasText(request.getExternalLink())
                ? " Lien externe fourni : " + request.getExternalLink().trim()
                : "";

        return AiMissionDescriptionResponse.builder()
                .suggestedTitle(title)
                .suggestedDescription(description + externalLinkNote)
                .insightSummary(insight)
                .externalApiUrl(GEMINI_API_BASE + geminiProperties.getModel() + ":generateContent")
                .modelLabel("B2U-HUB AI Assistant (fallback local)")
                .confidenceScore(0.72)
                .build();
    }

    private void validate(AiMissionDescriptionRequest request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new IllegalArgumentException("title is required for AI mission description");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
