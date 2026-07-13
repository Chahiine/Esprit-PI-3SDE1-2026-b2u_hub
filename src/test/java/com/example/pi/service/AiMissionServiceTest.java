package com.example.pi.service;

import com.example.pi.config.GeminiProperties;
import com.example.pi.dto.AiMissionDescriptionRequest;
import com.example.pi.dto.AiMissionDescriptionResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiMissionServiceTest {

    @Mock
    private GeminiProperties geminiProperties;

    @Mock
    private GeminiMissionClient geminiMissionClient;

    @InjectMocks
    private AiMissionService aiMissionService;

    private AiMissionDescriptionRequest request;

    @BeforeEach
    void setUp() {
        request = new AiMissionDescriptionRequest();
        request.setTitle("Stage développeur Angular");
        request.setCompanyName("TechNova SAS");
        request.setLocation("Tunis");
        request.setSkillsRequired("Angular, TypeScript");
        request.setDuration("3 mois");
        request.setExternalLink("https://technova.tn/careers");
    }

    @Test
    void suggestDescription_usesLocalFallbackWhenGeminiDisabled() {
        when(geminiProperties.isConfigured()).thenReturn(false);
        when(geminiProperties.getModel()).thenReturn("gemini-2.5-flash");

        AiMissionDescriptionResponse response = aiMissionService.suggestDescription(request);

        assertThat(response.getSuggestedTitle()).isEqualTo("Stage développeur Angular");
        assertThat(response.getSuggestedDescription()).contains("TechNova SAS");
        assertThat(response.getModelLabel()).contains("fallback local");
        assertThat(response.getExternalApiUrl()).contains("generativelanguage.googleapis.com");
    }

    @Test
    void suggestDescription_usesGeminiWhenConfigured() {
        AiMissionDescriptionResponse geminiResponse = AiMissionDescriptionResponse.builder()
                .suggestedTitle("Stage Angular B2U-HUB")
                .suggestedDescription("Description générée par Gemini")
                .insightSummary("Profil front-end")
                .externalApiUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent")
                .modelLabel("Gemini 2.5-flash (API externe)")
                .confidenceScore(0.9)
                .build();

        when(geminiProperties.isConfigured()).thenReturn(true);
        when(geminiMissionClient.suggestDescription(request)).thenReturn(geminiResponse);

        AiMissionDescriptionResponse response = aiMissionService.suggestDescription(request);

        assertThat(response.getSuggestedDescription()).isEqualTo("Description générée par Gemini");
        assertThat(response.getModelLabel()).contains("API externe");
    }

    @Test
    void suggestDescription_requiresTitle() {
        request.setTitle("  ");

        assertThatThrownBy(() -> aiMissionService.suggestDescription(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("title is required");
    }
}
