package com.example.pi.service;

import com.example.pi.dto.EvaluationCreateResponse;
import com.example.pi.entity.Evaluation;
import com.example.pi.repository.EvaluationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvaluationServiceTest {

    @Mock
    private EvaluationRepository evaluationRepository;

    @Mock
    private EmailNotificationService emailNotificationService;

    @InjectMocks
    private EvaluationService evaluationService;

    private Evaluation evaluation;

    @BeforeEach
    void setUp() {
        evaluation = Evaluation.builder()
                .studentName("Ines Gharbi")
                .studentEmail("ines@test.com")
                .enterpriseName("B2U Corp")
                .projectTitle("Plateforme web")
                .rating(5)
                .comment("Excellent")
                .projectDate(LocalDate.of(2026, 3, 1))
                .build();
    }

    @Test
    void create_savesEvaluationAndSendsEmail() {
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(inv -> {
            Evaluation e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        when(emailNotificationService.sendEvaluationToStudent(any()))
                .thenReturn("Notification simulee (mode console)");

        EvaluationCreateResponse response = evaluationService.create(evaluation);

        assertThat(response.getEvaluation().getId()).isEqualTo(1L);
        assertThat(response.isEmailSent()).isTrue();
        assertThat(response.getEmailMessage()).contains("simulee");
        verify(evaluationRepository).save(any(Evaluation.class));
    }

    @Test
    void getAll_returnsRepositoryResults() {
        when(evaluationRepository.findAll()).thenReturn(List.of(evaluation));

        assertThat(evaluationService.getAll()).hasSize(1);
    }

    @Test
    void getById_returnsEvaluation() {
        evaluation.setId(10L);
        when(evaluationRepository.findById(10L)).thenReturn(Optional.of(evaluation));

        assertThat(evaluationService.getById(10L).getStudentName()).isEqualTo("Ines Gharbi");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(evaluationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> evaluationService.getById(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void update_modifiesExistingEvaluation() {
        Evaluation existing = Evaluation.builder()
                .id(1L)
                .studentName("Old")
                .studentEmail("old@test.com")
                .enterpriseName("Old Corp")
                .projectTitle("Old project")
                .rating(3)
                .projectDate(LocalDate.of(2025, 1, 1))
                .build();

        when(evaluationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(evaluationRepository.save(any(Evaluation.class))).thenAnswer(inv -> inv.getArgument(0));

        Evaluation updated = evaluationService.update(1L, evaluation);

        assertThat(updated.getStudentName()).isEqualTo("Ines Gharbi");
        assertThat(updated.getRating()).isEqualTo(5);
    }

    @Test
    void delete_removesEvaluation() {
        evaluation.setId(5L);
        when(evaluationRepository.findById(5L)).thenReturn(Optional.of(evaluation));

        evaluationService.delete(5L);

        ArgumentCaptor<Evaluation> captor = ArgumentCaptor.forClass(Evaluation.class);
        verify(evaluationRepository).delete(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(5L);
    }

    @Test
    void create_rejectsInvalidEmail() {
        evaluation.setStudentEmail("invalid-email");

        assertThatThrownBy(() -> evaluationService.create(evaluation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email is invalid");
    }

    @Test
    void create_rejectsInvalidRating() {
        evaluation.setRating(6);

        assertThatThrownBy(() -> evaluationService.create(evaluation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Rating must be between 1 and 5");
    }

    @Test
    void create_rejectsMissingProjectDate() {
        evaluation.setProjectDate(null);

        assertThatThrownBy(() -> evaluationService.create(evaluation))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Project date is required");
    }
}
