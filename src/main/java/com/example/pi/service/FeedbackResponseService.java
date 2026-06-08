package com.example.pi.service;

import com.example.pi.dto.FeedbackResponseCreateRequest;
import com.example.pi.entity.FeedbackResponse;
import com.example.pi.repository.EvaluationRepository;
import com.example.pi.repository.FeedbackResponseRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedbackResponseService {

    private final FeedbackResponseRepository feedbackResponseRepository;
    private final EvaluationRepository evaluationRepository;

    public FeedbackResponseService(
            FeedbackResponseRepository feedbackResponseRepository,
            EvaluationRepository evaluationRepository) {
        this.feedbackResponseRepository = feedbackResponseRepository;
        this.evaluationRepository = evaluationRepository;
    }

    public FeedbackResponse create(FeedbackResponseCreateRequest request) {
        validate(request);

        if (!evaluationRepository.existsById(request.getEvaluationId())) {
            throw new IllegalArgumentException("Evaluation not found with id: " + request.getEvaluationId());
        }

        FeedbackResponse saved = feedbackResponseRepository.save(FeedbackResponse.builder()
                .evaluationId(request.getEvaluationId())
                .studentName(request.getStudentName().trim())
                .studentEmail(request.getStudentEmail().trim())
                .message(request.getMessage().trim())
                .createdAt(LocalDateTime.now())
                .build());

        return saved;
    }

    public List<FeedbackResponse> listByStudentEmail(String studentEmail) {
        if (!StringUtils.hasText(studentEmail)) {
            throw new IllegalArgumentException("studentEmail is required");
        }
        return feedbackResponseRepository.findByStudentEmailOrderByCreatedAtDesc(studentEmail.trim());
    }

    public List<FeedbackResponse> listByEvaluationId(Long evaluationId) {
        if (evaluationId == null) {
            throw new IllegalArgumentException("evaluationId is required");
        }
        return feedbackResponseRepository.findByEvaluationIdOrderByCreatedAtDesc(evaluationId);
    }

    private void validate(FeedbackResponseCreateRequest request) {
        if (request.getEvaluationId() == null) {
            throw new IllegalArgumentException("evaluationId is required");
        }
        if (!StringUtils.hasText(request.getStudentName())) {
            throw new IllegalArgumentException("studentName is required");
        }
        if (!StringUtils.hasText(request.getStudentEmail()) || !request.getStudentEmail().contains("@")) {
            throw new IllegalArgumentException("Valid studentEmail is required");
        }
        if (!StringUtils.hasText(request.getMessage()) || request.getMessage().trim().length() < 5) {
            throw new IllegalArgumentException("message must be at least 5 characters");
        }
    }
}
