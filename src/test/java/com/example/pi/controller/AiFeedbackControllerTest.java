package com.example.pi.controller;

import com.example.pi.dto.AiFeedbackRequest;
import com.example.pi.dto.AiFeedbackResponse;
import com.example.pi.service.AiFeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiFeedbackController.class)
@AutoConfigureMockMvc(addFilters = false)
class AiFeedbackControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AiFeedbackService aiFeedbackService;

    @Test
    void suggestFeedback_returnsOk() throws Exception {
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

        mockMvc.perform(post("/api/ai/suggest-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestedComment").value("Bon travail"));
    }

    @Test
    void suggestFeedback_returnsBadRequestOnValidationError() throws Exception {
        when(aiFeedbackService.suggestFeedback(any()))
                .thenThrow(new IllegalArgumentException("studentName is required"));

        AiFeedbackRequest request = new AiFeedbackRequest();
        request.setProjectTitle("Projet");

        mockMvc.perform(post("/api/ai/suggest-feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
