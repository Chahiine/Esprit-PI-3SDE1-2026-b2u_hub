package com.example.pi.controller;

import com.example.pi.dto.EvaluationCreateResponse;
import com.example.pi.entity.Evaluation;
import com.example.pi.service.EvaluationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EvaluationController.class)
@AutoConfigureMockMvc(addFilters = false)
class EvaluationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EvaluationService evaluationService;

    @Test
    void create_returnsOk() throws Exception {
        Evaluation evaluation = sampleEvaluation();
        evaluation.setId(1L);

        EvaluationCreateResponse response = EvaluationCreateResponse.builder()
                .evaluation(evaluation)
                .emailSent(true)
                .emailMessage("Notification simulee")
                .build();

        when(evaluationService.create(any(Evaluation.class))).thenReturn(response);

        mockMvc.perform(post("/api/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEvaluation())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailSent").value(true));
    }

    @Test
    void getAll_returnsList() throws Exception {
        when(evaluationService.getAll()).thenReturn(List.of(sampleEvaluation()));

        mockMvc.perform(get("/api/evaluations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentName").value("Youssef Trabelsi"));
    }

    @Test
    void getById_returnsEvaluation() throws Exception {
        Evaluation evaluation = sampleEvaluation();
        evaluation.setId(5L);
        when(evaluationService.getById(5L)).thenReturn(evaluation);

        mockMvc.perform(get("/api/evaluations/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectTitle").value("Module evaluation"));
    }

    @Test
    void getById_returnsNotFound() throws Exception {
        when(evaluationService.getById(99L))
                .thenThrow(new IllegalArgumentException("Evaluation not found"));

        mockMvc.perform(get("/api/evaluations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/evaluations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void update_returnsBadRequestOnValidationError() throws Exception {
        doThrow(new IllegalArgumentException("Invalid"))
                .when(evaluationService).update(eq(1L), any(Evaluation.class));

        mockMvc.perform(put("/api/evaluations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleEvaluation())))
                .andExpect(status().isBadRequest());
    }

    private Evaluation sampleEvaluation() {
        return Evaluation.builder()
                .studentName("Youssef Trabelsi")
                .studentEmail("youssef@test.com")
                .enterpriseName("TechStart")
                .projectTitle("Module evaluation")
                .rating(4)
                .comment("Bon niveau")
                .projectDate(LocalDate.of(2026, 4, 10))
                .build();
    }
}
