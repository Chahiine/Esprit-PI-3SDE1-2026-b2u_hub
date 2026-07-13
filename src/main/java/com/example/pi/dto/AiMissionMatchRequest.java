package com.example.pi.dto;

import lombok.Data;

@Data
public class AiMissionMatchRequest {
    private String studentName;
    private String studentEmail;
    private String skills;
    private String preferredLocation;
    private Integer maxResults;
}
