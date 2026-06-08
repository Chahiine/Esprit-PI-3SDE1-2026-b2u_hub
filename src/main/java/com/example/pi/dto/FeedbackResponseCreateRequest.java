package com.example.pi.dto;

import lombok.Data;

@Data
public class FeedbackResponseCreateRequest {
    private Long evaluationId;
    private String studentName;
    private String studentEmail;
    private String message;
}
