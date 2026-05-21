package com.example.pi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String studentName;

    /** E-mail pour notification (peut être vide sur anciennes lignes). */
    @Column
    private String studentEmail;

    @Column(nullable = false)
    private String enterpriseName;

    @Column(nullable = false)
    private String projectTitle;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 2000)
    private String comment;

    @Column(nullable = false)
    private LocalDate projectDate;
}
