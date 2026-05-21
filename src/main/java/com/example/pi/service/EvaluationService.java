package com.example.pi.service;

import com.example.pi.dto.EvaluationCreateResponse;
import com.example.pi.entity.Evaluation;
import com.example.pi.repository.EvaluationRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class EvaluationService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final EvaluationRepository evaluationRepository;
    private final EmailNotificationService emailNotificationService;

    public EvaluationService(
            EvaluationRepository evaluationRepository,
            EmailNotificationService emailNotificationService) {
        this.evaluationRepository = evaluationRepository;
        this.emailNotificationService = emailNotificationService;
    }

    public EvaluationCreateResponse create(Evaluation evaluation) {
        validateEvaluation(evaluation);
        Evaluation saved = evaluationRepository.save(evaluation);
        String emailMessage = emailNotificationService.sendEvaluationToStudent(saved);
        boolean emailSent = emailMessage.contains("envoye") || emailMessage.contains("envoyée")
                || emailMessage.contains("simulee");
        return EvaluationCreateResponse.builder()
                .evaluation(saved)
                .emailSent(emailSent)
                .emailMessage(emailMessage)
                .build();
    }

    public List<Evaluation> getAll() {
        return evaluationRepository.findAll();
    }

    public Evaluation getById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evaluation not found with id: " + id));
    }

    public Evaluation update(Long id, Evaluation payload) {
        validateEvaluation(payload);
        Evaluation existing = getById(id);

        existing.setStudentName(payload.getStudentName());
        existing.setStudentEmail(payload.getStudentEmail());
        existing.setEnterpriseName(payload.getEnterpriseName());
        existing.setProjectTitle(payload.getProjectTitle());
        existing.setRating(payload.getRating());
        existing.setComment(payload.getComment());
        existing.setProjectDate(payload.getProjectDate());

        return evaluationRepository.save(existing);
    }

    public void delete(Long id) {
        Evaluation existing = getById(id);
        evaluationRepository.delete(existing);
    }

    private void validateEvaluation(Evaluation evaluation) {
        if (!StringUtils.hasText(evaluation.getStudentName())) {
            throw new IllegalArgumentException("Student name is required");
        }
        if (!StringUtils.hasText(evaluation.getStudentEmail())) {
            throw new IllegalArgumentException("Student email is required");
        }
        if (!EMAIL_PATTERN.matcher(evaluation.getStudentEmail().trim()).matches()) {
            throw new IllegalArgumentException("Student email is invalid");
        }
        evaluation.setStudentEmail(evaluation.getStudentEmail().trim());
        if (!StringUtils.hasText(evaluation.getEnterpriseName())) {
            throw new IllegalArgumentException("Enterprise name is required");
        }
        if (!StringUtils.hasText(evaluation.getProjectTitle())) {
            throw new IllegalArgumentException("Project title is required");
        }
        if (evaluation.getRating() == null || evaluation.getRating() < 1 || evaluation.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (evaluation.getProjectDate() == null) {
            throw new IllegalArgumentException("Project date is required");
        }
    }
}
