package com.example.pi.service;

import com.example.pi.config.GeminiProperties;
import com.example.pi.dto.AiMissionMatchRequest;
import com.example.pi.dto.AiMissionMatchResponse;
import com.example.pi.dto.MissionMatchItemDto;
import com.example.pi.entity.Mission;
import com.example.pi.entity.MissionStatus;
import com.example.pi.entity.User;
import com.example.pi.repository.MissionRepository;
import com.example.pi.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Matching intelligent étudiant ↔ missions (différent de la génération de texte feedback/description).
 * Score de compatibilité, forces, lacunes et recommandations.
 */
@Service
public class AiMissionMatchService {

    private static final Logger log = LoggerFactory.getLogger(AiMissionMatchService.class);

    private final GeminiProperties geminiProperties;
    private final GeminiMissionMatchClient geminiMissionMatchClient;
    private final MissionRepository missionRepository;
    private final UserRepository userRepository;

    public AiMissionMatchService(
            GeminiProperties geminiProperties,
            GeminiMissionMatchClient geminiMissionMatchClient,
            MissionRepository missionRepository,
            UserRepository userRepository) {
        this.geminiProperties = geminiProperties;
        this.geminiMissionMatchClient = geminiMissionMatchClient;
        this.missionRepository = missionRepository;
        this.userRepository = userRepository;
    }

    public AiMissionMatchResponse matchMissions(AiMissionMatchRequest request) {
        validate(request);

        String studentName = request.getStudentName().trim();
        String skills = resolveSkills(request);
        String location = hasText(request.getPreferredLocation())
                ? request.getPreferredLocation().trim()
                : null;
        int maxResults = request.getMaxResults() != null && request.getMaxResults() > 0
                ? Math.min(request.getMaxResults(), 10)
                : 5;

        List<Mission> openMissions = missionRepository.findByStatus(MissionStatus.OPEN);
        if (openMissions.isEmpty()) {
            return AiMissionMatchResponse.builder()
                    .studentSummary(buildStudentSummary(studentName, skills, location))
                    .matches(List.of())
                    .globalInsight("Aucune mission ouverte en base. Publiez des missions via POST /api/missions.")
                    .modelLabel("B2U-HUB AI Matcher")
                    .externalApiUrl(geminiApiUrl())
                    .confidenceScore(0.5)
                    .build();
        }

        if (geminiProperties.isConfigured()) {
            try {
                return geminiMissionMatchClient.matchMissions(
                        studentName, skills, location, maxResults, openMissions);
            } catch (Exception e) {
                log.warn("Gemini matching unavailable, using local scorer: {}", e.getMessage());
            }
        }

        return matchLocally(studentName, skills, location, maxResults, openMissions);
    }

    private AiMissionMatchResponse matchLocally(
            String studentName,
            String skills,
            String location,
            int maxResults,
            List<Mission> missions) {

        Set<String> studentTokens = tokenize(skills);

        List<MissionMatchItemDto> ranked = missions.stream()
                .map(mission -> scoreMission(mission, studentTokens, location))
                .sorted(Comparator.comparingInt(MissionMatchItemDto::getMatchScore).reversed())
                .limit(maxResults)
                .toList();

        String insight = ranked.isEmpty()
                ? "Aucune mission ne correspond suffisamment au profil."
                : String.format(
                "%d mission(s) analysée(s) — meilleur score %d%% sur « %s ».",
                ranked.size(),
                ranked.get(0).getMatchScore(),
                ranked.get(0).getTitle());

        return AiMissionMatchResponse.builder()
                .studentSummary(buildStudentSummary(studentName, skills, location))
                .matches(ranked)
                .globalInsight(insight)
                .modelLabel("B2U-HUB AI Matcher (analyse compétences + localisation)")
                .externalApiUrl(geminiApiUrl())
                .confidenceScore(0.78)
                .build();
    }

    private MissionMatchItemDto scoreMission(Mission mission, Set<String> studentTokens, String preferredLocation) {
        Set<String> missionTokens = tokenize(mission.getSkillsRequired());
        missionTokens.addAll(tokenize(mission.getTitle()));
        missionTokens.addAll(tokenize(mission.getDescription()));

        List<String> strengths = new ArrayList<>();
        List<String> gaps = new ArrayList<>();

        for (String token : missionTokens) {
            if (studentTokens.contains(token)) {
                strengths.add("Compétence alignée : " + token);
            }
        }

        for (String token : missionTokens) {
            if (!studentTokens.contains(token) && token.length() > 2) {
                gaps.add("À renforcer : " + token);
                if (gaps.size() >= 3) break;
            }
        }

        int overlap = strengths.size();
        int missionSize = Math.max(missionTokens.size(), 1);
        int score = (int) Math.round((overlap * 100.0) / missionSize);
        score = Math.min(100, Math.max(15, score + overlap * 8));

        if (hasText(preferredLocation) && hasText(mission.getLocation())
                && mission.getLocation().toLowerCase().contains(preferredLocation.toLowerCase())) {
            score = Math.min(100, score + 12);
            strengths.add("Localisation compatible : " + mission.getLocation());
        }

        if (strengths.isEmpty()) {
            strengths.add("Profil généraliste — mission ouverte à l'apprentissage");
        }
        if (gaps.isEmpty()) {
            gaps.add("Aucune lacune majeure détectée sur les compétences listées");
        }

        String level = score >= 75 ? "EXCELLENT" : score >= 50 ? "BON" : "PARTIEL";
        String recommendation = score >= 75
                ? "Candidature fortement recommandée — profil très aligné."
                : score >= 50
                ? "Bonne opportunité — compléter les compétences manquantes avant de postuler."
                : "Mission possible en montée en compétences — prévoir un accompagnement.";

        return MissionMatchItemDto.builder()
                .missionId(mission.getId())
                .title(mission.getTitle())
                .companyName(mission.getCompanyName())
                .location(mission.getLocation())
                .skillsRequired(mission.getSkillsRequired())
                .matchScore(score)
                .matchLevel(level)
                .strengths(strengths.stream().limit(4).toList())
                .gaps(gaps.stream().limit(3).toList())
                .recommendation(recommendation)
                .build();
    }

    private String resolveSkills(AiMissionMatchRequest request) {
        if (hasText(request.getSkills())) {
            return request.getSkills().trim();
        }
        if (hasText(request.getStudentEmail())) {
            Optional<User> user = userRepository.findByEmail(request.getStudentEmail().trim());
            if (user.isPresent() && hasText(user.get().getSkills())) {
                return user.get().getSkills().trim();
            }
        }
        throw new IllegalArgumentException("skills is required (or register student with skills in /api/users/register)");
    }

    private String buildStudentSummary(String name, String skills, String location) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" — compétences : ").append(skills);
        if (hasText(location)) {
            sb.append(" — préférence : ").append(location);
        }
        return sb.toString();
    }

    private Set<String> tokenize(String text) {
        if (!hasText(text)) {
            return new HashSet<>();
        }
        return Arrays.stream(text.toLowerCase().split("[,;\\s/|+]+"))
                .map(String::trim)
                .filter(s -> s.length() > 2)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validate(AiMissionMatchRequest request) {
        if (!StringUtils.hasText(request.getStudentName())) {
            throw new IllegalArgumentException("studentName is required for AI mission matching");
        }
    }

    private String geminiApiUrl() {
        return "https://generativelanguage.googleapis.com/v1beta/models/"
                + geminiProperties.getModel() + ":generateContent";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
