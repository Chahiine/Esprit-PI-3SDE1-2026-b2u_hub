package com.example.pi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiMissionDescriptionResponse {
    private String suggestedDescription;
    private String suggestedTitle;
    private String insightSummary;
    private String externalApiUrl;
    private String modelLabel;
    private double confidenceScore;
}
