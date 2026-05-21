package com.example.pi.dto;

import com.example.pi.entity.Evaluation;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EvaluationCreateResponse {
    private Evaluation evaluation;
    private boolean emailSent;
    private String emailMessage;
}
