package com.example.pi.dto;

import lombok.Data;

@Data
public class AiMissionDescriptionRequest {
    private String title;
    private String companyName;
    private String location;
    private String skillsRequired;
    private String duration;
    private String externalLink;
}
