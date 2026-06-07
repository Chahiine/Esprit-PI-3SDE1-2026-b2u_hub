package com.example.pi.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 3000)
    private String description;

    @Column(nullable = false)
    private String enterpriseName;

    // Comma-separated: "Java, Spring Boot, Angular"
    private String skillsRequired;

    // OPEN, IN_PROGRESS, CLOSED
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MissionStatus status = MissionStatus.OPEN;

    // BEGINNER, INTERMEDIATE, EXPERT
    private String level;

    private Double budget;

    private LocalDate deadline;

    // Category maps to the template's badge (Education, UI/UX, etc.)
    private String category;

    // Who created this mission (company user id)
    private Long createdByUserId;

    @Builder.Default
    private LocalDate createdAt = LocalDate.now();

    public enum MissionStatus { OPEN, IN_PROGRESS, CLOSED }
}
