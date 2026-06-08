package com.example.pi.controller;

import com.example.pi.dto.EvaluationCreateResponse;
import com.example.pi.entity.Evaluation;
import com.example.pi.service.EvaluationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationControllerTest {

    @Mock
    private EvaluationService evaluationService;

    @InjectMocks
    private EvaluationController controller;

    @Test
    void create_returnsOk() {
        Evaluation evaluation = sampleEvaluation();
        evaluation.setId(1L);

        EvaluationCreateResponse response = EvaluationCreateResponse.builder()
                .evaluation(evaluation)
                .emailSent(true)
                .emailMessage("Notification simulee")
                .build();

        when(evaluationService.create(any(Evaluation.class))).thenReturn(response);

        ResponseEntity<?> result = controller.create(sampleEvaluation());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(EvaluationCreateResponse.class);
    }

    @Test
    void getAll_returnsList() {
        when(evaluationService.getAll()).thenReturn(List.of(sampleEvaluation()));

        List<Evaluation> result = controller.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStudentName()).isEqualTo("Youssef Trabelsi");
    }

    @Test
    void getById_returnsEvaluation() {
        Evaluation evaluation = sampleEvaluation();
        evaluation.setId(5L);
        when(evaluationService.getById(5L)).thenReturn(evaluation);

        ResponseEntity<?> result = controller.getById(5L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(evaluation);
    }

    @Test
    void getById_returnsNotFound() {
        when(evaluationService.getById(99L))
                .thenThrow(new IllegalArgumentException("Evaluation not found"));

        ResponseEntity<?> result = controller.getById(99L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> result = controller.delete(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void update_returnsBadRequestOnValidationError() {
        doThrow(new IllegalArgumentException("Invalid"))
                .when(evaluationService).update(eq(1L), any(Evaluation.class));

        ResponseEntity<?> result = controller.update(1L, sampleEvaluation());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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
