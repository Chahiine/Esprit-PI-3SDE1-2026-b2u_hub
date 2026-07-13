package com.example.pi.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private String companyName;

    private String location;

    @Column(length = 1000)
    private String skillsRequired;

    private String duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MissionStatus status;

    /** E-mail de l'entreprise qui publie la mission. */
    private String companyEmail;

    /** Lien externe optionnel (offre LinkedIn, site carrière, etc.). */
    @Column(length = 2000)
    private String externalLink;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = MissionStatus.OPEN;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
