package com.example.pi.controller;

import com.example.pi.dto.AiFeedbackRequest;
import com.example.pi.dto.AiFeedbackResponse;
import com.example.pi.service.AiFeedbackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiFeedbackControllerTest {

    @Mock
    private AiFeedbackService aiFeedbackService;

    @InjectMocks
    private AiFeedbackController controller;

    @Test
    void suggestFeedback_returnsOk() {
        AiFeedbackRequest request = new AiFeedbackRequest();
        request.setStudentName("Lucas");
        request.setProjectTitle("Projet PI");
        request.setRating(4);

        AiFeedbackResponse response = AiFeedbackResponse.builder()
                .suggestedComment("Bon travail")
                .strengths(List.of("Communication"))
                .improvements(List.of())
                .insightSummary("Profil standard")
                .confidenceScore(0.85)
                .modelLabel("B2U-HUB AI")
                .build();

        when(aiFeedbackService.suggestFeedback(any())).thenReturn(response);

        ResponseEntity<?> result = controller.suggestFeedback(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(AiFeedbackResponse.class);
        assertThat(((AiFeedbackResponse) result.getBody()).getSuggestedComment()).isEqualTo("Bon travail");
    }

    @Test
    void suggestFeedback_returnsBadRequestOnValidationError() {
        when(aiFeedbackService.suggestFeedback(any()))
                .thenThrow(new IllegalArgumentException("studentName is required"));

        AiFeedbackRequest request = new AiFeedbackRequest();
        request.setProjectTitle("Projet");

        ResponseEntity<?> result = controller.suggestFeedback(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo("studentName is required");
    }
}
