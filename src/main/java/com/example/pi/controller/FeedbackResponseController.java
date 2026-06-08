package com.example.pi.controller;

import com.example.pi.dto.FeedbackResponseCreateRequest;
import com.example.pi.entity.FeedbackResponse;
import com.example.pi.service.FeedbackResponseService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback-responses")
@CrossOrigin("*")
public class FeedbackResponseController {

    private final FeedbackResponseService feedbackResponseService;

    public FeedbackResponseController(FeedbackResponseService feedbackResponseService) {
        this.feedbackResponseService = feedbackResponseService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody FeedbackResponseCreateRequest request) {
        try {
            return ResponseEntity.ok(feedbackResponseService.create(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String studentEmail,
            @RequestParam(required = false) Long evaluationId) {
        try {
            if (StringUtils.hasText(studentEmail)) {
                return ResponseEntity.ok(feedbackResponseService.listByStudentEmail(studentEmail));
            }
            if (evaluationId != null) {
                return ResponseEntity.ok(feedbackResponseService.listByEvaluationId(evaluationId));
            }
            return ResponseEntity.badRequest().body("studentEmail or evaluationId is required");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
