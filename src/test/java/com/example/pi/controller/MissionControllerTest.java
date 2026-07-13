package com.example.pi.controller;

import com.example.pi.entity.Mission;
import com.example.pi.entity.MissionStatus;
import com.example.pi.service.MissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionControllerTest {

    @Mock
    private MissionService missionService;

    @InjectMocks
    private MissionController controller;

    @Test
    void create_returnsOk() {
        Mission mission = sampleMission();
        mission.setId(1L);
        when(missionService.create(any(Mission.class))).thenReturn(mission);

        ResponseEntity<?> result = controller.create(sampleMission());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isInstanceOf(Mission.class);
    }

    @Test
    void search_returnsList() {
        when(missionService.search(any(), any(), any(), any(), any()))
                .thenReturn(List.of(sampleMission()));

        ResponseEntity<?> result = controller.search("angular", "Tunis", MissionStatus.OPEN, "Spring", "TechNova");

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat((List<?>) result.getBody()).hasSize(1);
    }

    @Test
    void getById_returnsNotFound() {
        when(missionService.getById(99L))
                .thenThrow(new IllegalArgumentException("Mission not found"));

        ResponseEntity<?> result = controller.getById(99L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void update_returnsBadRequestOnValidationError() {
        doThrow(new IllegalArgumentException("Invalid"))
                .when(missionService).update(eq(1L), any(Mission.class));

        ResponseEntity<?> result = controller.update(1L, sampleMission());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void delete_returnsNoContent() {
        ResponseEntity<Void> result = controller.delete(1L);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    private Mission sampleMission() {
        return Mission.builder()
                .title("Stage Spring Boot")
                .description("Backend API")
                .companyName("InnoSoft")
                .location("Tunis")
                .skillsRequired("Java, Spring Boot")
                .duration("4 mois")
                .status(MissionStatus.OPEN)
                .externalLink("https://innosoft.tn/missions/1")
                .build();
    }
}
