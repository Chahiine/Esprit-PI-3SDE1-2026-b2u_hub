package com.example.pi.repository;

import com.example.pi.entity.FeedbackResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackResponseRepository extends JpaRepository<FeedbackResponse, Long> {

    List<FeedbackResponse> findByStudentEmailOrderByCreatedAtDesc(String studentEmail);

    List<FeedbackResponse> findByEvaluationIdOrderByCreatedAtDesc(Long evaluationId);
}
