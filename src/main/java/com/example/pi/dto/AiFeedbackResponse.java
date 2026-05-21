package com.example.pi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiFeedbackResponse {
    private String suggestedComment;
    private List<String> strengths;
    private List<String> improvements;
    private String insightSummary;
    private double confidenceScore;
    private String modelLabel;
}
