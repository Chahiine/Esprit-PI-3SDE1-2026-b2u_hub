package com.example.pi.service;

import com.example.pi.config.GeminiProperties;
import com.example.pi.dto.AiMissionMatchRequest;
import com.example.pi.dto.AiMissionMatchResponse;
import com.example.pi.entity.Mission;
import com.example.pi.entity.MissionStatus;
import com.example.pi.entity.Role;
import com.example.pi.entity.User;
import com.example.pi.repository.MissionRepository;
import com.example.pi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiMissionMatchServiceTest {

    @Mock
    private GeminiProperties geminiProperties;

    @Mock
    private GeminiMissionMatchClient geminiMissionMatchClient;

    @Mock
    private MissionRepository missionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AiMissionMatchService aiMissionMatchService;

    private AiMissionMatchRequest request;
    private Mission mission;

    @BeforeEach
    void setUp() {
        request = new AiMissionMatchRequest();
        request.setStudentName("Chahine Sassi");
        request.setSkills("Angular, Spring Boot, PostgreSQL");
        request.setPreferredLocation("Tunis");
        request.setMaxResults(3);

        mission = Mission.builder()
                .id(1L)
                .title("Stage Angular")
                .companyName("TechNova")
                .location("Tunis")
                .skillsRequired("Angular, TypeScript")
                .status(MissionStatus.OPEN)
                .build();
    }

    @Test
    void matchMissions_scoresLocallyWhenGeminiDisabled() {
        when(geminiProperties.isConfigured()).thenReturn(false);
        when(geminiProperties.getModel()).thenReturn("gemini-2.5-flash");
        when(missionRepository.findByStatus(MissionStatus.OPEN)).thenReturn(List.of(mission));

        AiMissionMatchResponse response = aiMissionMatchService.matchMissions(request);

        assertThat(response.getMatches()).hasSize(1);
        assertThat(response.getMatches().get(0).getMatchScore()).isGreaterThan(0);
        assertThat(response.getModelLabel()).contains("Matcher");
    }

    @Test
    void matchMissions_loadsSkillsFromUserWhenEmailProvided() {
        request.setSkills(null);
        request.setStudentEmail("chahine@test.com");

        User user = User.builder()
                .firstName("Chahine")
                .lastName("Sassi")
                .email("chahine@test.com")
                .password("secret1")
                .role(Role.STUDENT)
                .skills("Java, Spring Boot")
                .build();

        when(userRepository.findByEmail("chahine@test.com")).thenReturn(Optional.of(user));
        when(geminiProperties.isConfigured()).thenReturn(false);
        when(geminiProperties.getModel()).thenReturn("gemini-2.5-flash");
        when(missionRepository.findByStatus(MissionStatus.OPEN)).thenReturn(List.of(mission));

        AiMissionMatchResponse response = aiMissionMatchService.matchMissions(request);

        assertThat(response.getStudentSummary()).contains("Java");
    }

    @Test
    void matchMissions_requiresStudentName() {
        request.setStudentName(" ");

        assertThatThrownBy(() -> aiMissionMatchService.matchMissions(request))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
