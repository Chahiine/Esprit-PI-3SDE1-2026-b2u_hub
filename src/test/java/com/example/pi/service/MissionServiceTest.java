package com.example.pi.service;

import com.example.pi.entity.Mission;
import com.example.pi.entity.MissionStatus;
import com.example.pi.repository.MissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MissionServiceTest {

    @Mock
    private MissionRepository missionRepository;

    @InjectMocks
    private MissionService missionService;

    private Mission mission;

    @BeforeEach
    void setUp() {
        mission = Mission.builder()
                .title("Stage développeur Angular")
                .description("Mission front-end")
                .companyName("TechNova SAS")
                .location("Tunis")
                .skillsRequired("Angular, TypeScript")
                .duration("3 mois")
                .status(MissionStatus.OPEN)
                .companyEmail("hr@technova.tn")
                .externalLink("https://technova.tn/careers/angular")
                .build();
    }

    @Test
    void create_savesMission() {
        when(missionRepository.save(any(Mission.class))).thenAnswer(inv -> {
            Mission m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });

        Mission saved = missionService.create(mission);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getTitle()).isEqualTo("Stage développeur Angular");
        verify(missionRepository).save(any(Mission.class));
    }

    @Test
    void create_rejectsInvalidExternalLink() {
        mission.setExternalLink("not-a-valid-url");

        assertThatThrownBy(() -> missionService.create(mission))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("External link");
    }

    @Test
    void search_returnsFilteredMissions() {
        when(missionRepository.findAll(any(Specification.class))).thenReturn(List.of(mission));

        List<Mission> result = missionService.search("angular", "Tunis", MissionStatus.OPEN, "TypeScript", "TechNova");

        assertThat(result).hasSize(1);
    }

    @Test
    void getById_returnsMission() {
        mission.setId(3L);
        when(missionRepository.findById(3L)).thenReturn(Optional.of(mission));

        assertThat(missionService.getById(3L).getCompanyName()).isEqualTo("TechNova SAS");
    }

    @Test
    void update_changesFields() {
        mission.setId(2L);
        when(missionRepository.findById(2L)).thenReturn(Optional.of(mission));
        when(missionRepository.save(any(Mission.class))).thenAnswer(inv -> inv.getArgument(0));

        Mission payload = Mission.builder()
                .title("Stage Angular avancé")
                .description("Mission mise à jour")
                .companyName("TechNova SAS")
                .location("Sfax")
                .skillsRequired("Angular, RxJS")
                .duration("6 mois")
                .status(MissionStatus.IN_PROGRESS)
                .externalLink("https://technova.tn/jobs/2")
                .build();

        Mission updated = missionService.update(2L, payload);

        assertThat(updated.getTitle()).isEqualTo("Stage Angular avancé");
        assertThat(updated.getStatus()).isEqualTo(MissionStatus.IN_PROGRESS);
        assertThat(updated.getLocation()).isEqualTo("Sfax");
    }

    @Test
    void delete_removesMission() {
        mission.setId(4L);
        when(missionRepository.findById(4L)).thenReturn(Optional.of(mission));

        missionService.delete(4L);

        verify(missionRepository).delete(mission);
    }
}
