package com.example.pi.service;

import com.example.pi.entity.Mission;
import com.example.pi.entity.MissionStatus;
import com.example.pi.repository.MissionRepository;
import com.example.pi.repository.MissionSpecifications;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.List;

@Service
public class MissionService {

    private final MissionRepository missionRepository;

    public MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    public Mission create(Mission mission) {
        validateMission(mission);
        if (mission.getStatus() == null) {
            mission.setStatus(MissionStatus.OPEN);
        }
        return missionRepository.save(mission);
    }

    public List<Mission> search(
            String keyword,
            String location,
            MissionStatus status,
            String skills,
            String companyName) {
        return missionRepository.findAll(
                MissionSpecifications.withFilters(keyword, location, status, skills, companyName)
        );
    }

    public Mission getById(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found with id: " + id));
    }

    public Mission update(Long id, Mission payload) {
        validateMission(payload);
        Mission existing = getById(id);

        existing.setTitle(payload.getTitle());
        existing.setDescription(payload.getDescription());
        existing.setCompanyName(payload.getCompanyName());
        existing.setLocation(payload.getLocation());
        existing.setSkillsRequired(payload.getSkillsRequired());
        existing.setDuration(payload.getDuration());
        existing.setStatus(payload.getStatus() != null ? payload.getStatus() : existing.getStatus());
        existing.setCompanyEmail(payload.getCompanyEmail());
        existing.setExternalLink(payload.getExternalLink());

        return missionRepository.save(existing);
    }

    public void delete(Long id) {
        Mission existing = getById(id);
        missionRepository.delete(existing);
    }

    private void validateMission(Mission mission) {
        if (!StringUtils.hasText(mission.getTitle())) {
            throw new IllegalArgumentException("Mission title is required");
        }
        if (!StringUtils.hasText(mission.getCompanyName())) {
            throw new IllegalArgumentException("Company name is required");
        }
        if (StringUtils.hasText(mission.getCompanyEmail())
                && !mission.getCompanyEmail().contains("@")) {
            throw new IllegalArgumentException("Company email is invalid");
        }
        if (StringUtils.hasText(mission.getExternalLink())) {
            validateExternalLink(mission.getExternalLink().trim());
            mission.setExternalLink(mission.getExternalLink().trim());
        }
        mission.setTitle(mission.getTitle().trim());
        mission.setCompanyName(mission.getCompanyName().trim());
    }

    private void validateExternalLink(String link) {
        try {
            URI uri = URI.create(link);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("External link must start with http:// or https://");
            }
            if (!StringUtils.hasText(uri.getHost())) {
                throw new IllegalArgumentException("External link host is invalid");
            }
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("External link")) {
                throw e;
            }
            throw new IllegalArgumentException("External link is invalid");
        }
    }
}
