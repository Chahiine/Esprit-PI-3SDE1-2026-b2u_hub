package com.example.pi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiMissionMatchResponse {
    private String studentSummary;
    private List<MissionMatchItemDto> matches;
    private String globalInsight;
    private String modelLabel;
    private String externalApiUrl;
    private double confidenceScore;
}
