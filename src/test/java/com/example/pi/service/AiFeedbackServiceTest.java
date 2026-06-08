package com.example.pi.service;

import com.example.pi.config.GeminiProperties;
import com.example.pi.dto.AiFeedbackRequest;
import com.example.pi.dto.AiFeedbackResponse;
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
class AiFeedbackServiceTest {

    @Mock
    private GeminiProperties geminiProperties;

    @Mock
    private GeminiFeedbackClient geminiFeedbackClient;

    @InjectMocks
    private AiFeedbackService aiFeedbackService;

    private AiFeedbackRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new AiFeedbackRequest();
        validRequest.setStudentName("Lucas Martin");
        validRequest.setProjectTitle("Application mobile");
        validRequest.setRating(4);
        validRequest.setQuality(5);
        validRequest.setCommunication(4);
        validRequest.setProfessionalism(4);
    }

    @Test
    void suggestFeedback_usesLocalEngine_whenGeminiNotConfigured() {
        when(geminiProperties.isConfigured()).thenReturn(false);

        AiFeedbackResponse response = aiFeedbackService.suggestFeedback(validRequest);

        assertThat(response.getSuggestedComment()).contains("Lucas Martin");
        assertThat(response.getSuggestedComment()).contains("Application mobile");
        assertThat(response.getStrengths()).isNotEmpty();
        assertThat(response.getModelLabel()).contains("B2U-HUB AI Assistant");
        assertThat(response.getConfidenceScore()).isBetween(0.0, 1.0);
    }

    @Test
    void suggestFeedback_usesGemini_whenConfigured() {
        AiFeedbackResponse geminiResponse = AiFeedbackResponse.builder()
                .suggestedComment("Excellent travail")
                .modelLabel("Gemini 2.5 Flash")
                .confidenceScore(0.9)
                .build();

        when(geminiProperties.isConfigured()).thenReturn(true);
        when(geminiFeedbackClient.suggestFeedback(validRequest)).thenReturn(geminiResponse);

        AiFeedbackResponse response = aiFeedbackService.suggestFeedback(validRequest);

        assertThat(response.getSuggestedComment()).isEqualTo("Excellent travail");
        assertThat(response.getModelLabel()).contains("Gemini");
    }

    @Test
    void suggestFeedback_fallsBackToLocal_whenGeminiFails() {
        when(geminiProperties.isConfigured()).thenReturn(true);
        when(geminiFeedbackClient.suggestFeedback(validRequest))
                .thenThrow(new IllegalStateException("API unavailable"));

        AiFeedbackResponse response = aiFeedbackService.suggestFeedback(validRequest);

        assertThat(response.getSuggestedComment()).contains("Lucas Martin");
        assertThat(response.getModelLabel()).contains("B2U-HUB AI Assistant");
    }

    @Test
    void suggestFeedback_addsImprovements_forLowRatings() {
        when(geminiProperties.isConfigured()).thenReturn(false);
        validRequest.setQuality(1);
        validRequest.setCommunication(2);
        validRequest.setProfessionalism(1);
        validRequest.setRating(2);

        AiFeedbackResponse response = aiFeedbackService.suggestFeedback(validRequest);

        assertThat(response.getImprovements()).isNotEmpty();
        assertThat(response.getSuggestedComment()).contains("renforcer");
    }

    @Test
    void suggestFeedback_rejectsMissingStudentName() {
        validRequest.setStudentName("");

        assertThatThrownBy(() -> aiFeedbackService.suggestFeedback(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("studentName");
    }

    @Test
    void suggestFeedback_rejectsMissingProjectTitle() {
        validRequest.setProjectTitle(null);

        assertThatThrownBy(() -> aiFeedbackService.suggestFeedback(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("projectTitle");
    }

    @Test
    void suggestFeedback_rejectsMissingRating() {
        validRequest.setRating(null);

        assertThatThrownBy(() -> aiFeedbackService.suggestFeedback(validRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rating");
    }
}
