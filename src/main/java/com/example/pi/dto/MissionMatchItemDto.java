package com.example.pi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MissionMatchItemDto {
    private Long missionId;
    private String title;
    private String companyName;
    private String location;
    private String skillsRequired;
    private int matchScore;
    private String matchLevel;
    private List<String> strengths;
    private List<String> gaps;
    private String recommendation;
}
