package com.example.pi.dto;

import com.example.pi.entity.Mission.MissionStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MissionDto {
    private Long   id;
    private String title;
    private String description;
    private String enterpriseName;
    private String skillsRequired;
    private MissionStatus status;
    private String level;
    private Double budget;
    private LocalDate deadline;
    private String category;
    private Long   createdByUserId;
    private LocalDate createdAt;
}
