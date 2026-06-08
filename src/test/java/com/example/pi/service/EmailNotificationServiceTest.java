package com.example.pi.service;

import com.example.pi.entity.Evaluation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private Evaluation evaluation;

    @BeforeEach
    void setUp() {
        evaluation = Evaluation.builder()
                .studentName("Samira Benali")
                .studentEmail("samira@test.com")
                .enterpriseName("Esprit")
                .projectTitle("PFE B2U")
                .rating(4)
                .comment("Tres bon travail")
                .projectDate(LocalDate.of(2026, 5, 1))
                .build();

        ReflectionTestUtils.setField(emailNotificationService, "mailEnabled", true);
        ReflectionTestUtils.setField(emailNotificationService, "mailMode", "console");
        ReflectionTestUtils.setField(emailNotificationService, "mailFrom", "test@b2u-hub.com");
    }

    @Test
    void sendEvaluationToStudent_simulatesInConsoleMode() {
        String result = emailNotificationService.sendEvaluationToStudent(evaluation);

        assertThat(result).contains("simulee");
        assertThat(result).contains("samira@test.com");
    }

    @Test
    void sendEvaluationToStudent_returnsMessageWhenEmailMissing() {
        evaluation.setStudentEmail("");

        String result = emailNotificationService.sendEvaluationToStudent(evaluation);

        assertThat(result).contains("manquant");
    }

    @Test
    void sendEvaluationToStudent_skipsWhenMailDisabled() {
        ReflectionTestUtils.setField(emailNotificationService, "mailEnabled", false);

        String result = emailNotificationService.sendEvaluationToStudent(evaluation);

        assertThat(result).contains("desactive");
    }

    @Test
    void sendTestEmail_simulatesInConsoleMode() {
        String result = emailNotificationService.sendTestEmail("test@example.com");

        assertThat(result).contains("simulee");
    }

    @Test
    void sendTestEmail_returnsDisabledMessage() {
        ReflectionTestUtils.setField(emailNotificationService, "mailEnabled", false);

        String result = emailNotificationService.sendTestEmail("test@example.com");

        assertThat(result).contains("desactive");
    }

    @Test
    void constructor_acceptsEmptyMailSender() {
        EmailNotificationService service = new EmailNotificationService(Optional.empty());

        assertThat(service).isNotNull();
    }
}
