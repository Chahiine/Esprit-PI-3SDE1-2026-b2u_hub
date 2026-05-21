package com.example.pi.dto;

import lombok.Data;

@Data
public class AiFeedbackRequest {
    private String studentName;
    private String projectTitle;
    private Integer rating;
    private Integer quality;
    private Integer communication;
    private Integer professionalism;
}
