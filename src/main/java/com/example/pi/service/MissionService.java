package com.example.pi.service;

import com.example.pi.dto.MissionDto;
import com.example.pi.entity.Mission;
import com.example.pi.entity.Mission.MissionStatus;
import com.example.pi.repository.MissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MissionService {

    private final MissionRepository missionRepository;

    // ── CREATE ────────────────────────────────────────────────
    public Mission create(MissionDto dto) {
        validate(dto);
        Mission mission = Mission.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .enterpriseName(dto.getEnterpriseName())
                .skillsRequired(dto.getSkillsRequired())
                .status(dto.getStatus() != null ? dto.getStatus() : MissionStatus.OPEN)
                .level(dto.getLevel())
                .budget(dto.getBudget())
                .deadline(dto.getDeadline())
                .category(dto.getCategory())
                .createdByUserId(dto.getCreatedByUserId())
                .createdAt(LocalDate.now())
                .build();
        return missionRepository.save(mission);
    }

    // ── GET ALL ───────────────────────────────────────────────
    public List<Mission> getAll() {
        return missionRepository.findAll();
    }

    // ── GET OPEN MISSIONS (public list) ───────────────────────
    public List<Mission> getOpen() {
        return missionRepository.findByStatus(MissionStatus.OPEN);
    }

    // ── GET BY ID ─────────────────────────────────────────────
    public Mission getById(Long id) {
        return missionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mission not found: " + id));
    }

    // ── GET BY USER ───────────────────────────────────────────
    public List<Mission> getByUser(Long userId) {
        return missionRepository.findByCreatedByUserId(userId);
    }

    // ── SEARCH ────────────────────────────────────────────────
    public List<Mission> search(String keyword) {
        if (!StringUtils.hasText(keyword)) return getAll();
        return missionRepository.search(keyword.trim());
    }

    // ── UPDATE ────────────────────────────────────────────────
    public Mission update(Long id, MissionDto dto) {
        validate(dto);
        Mission existing = getById(id);
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setEnterpriseName(dto.getEnterpriseName());
        existing.setSkillsRequired(dto.getSkillsRequired());
        if (dto.getStatus() != null) existing.setStatus(dto.getStatus());
        existing.setLevel(dto.getLevel());
        existing.setBudget(dto.getBudget());
        existing.setDeadline(dto.getDeadline());
        existing.setCategory(dto.getCategory());
        return missionRepository.save(existing);
    }

    // ── DELETE ────────────────────────────────────────────────
    public void delete(Long id) {
        if (!missionRepository.existsById(id))
            throw new IllegalArgumentException("Mission not found: " + id);
        missionRepository.deleteById(id);
    }

    // ── STATS ─────────────────────────────────────────────────
    public java.util.Map<String, Long> getStats() {
        return java.util.Map.of(
                "total",      missionRepository.count(),
                "open",       missionRepository.countByStatus(MissionStatus.OPEN),
                "inProgress", missionRepository.countByStatus(MissionStatus.IN_PROGRESS),
                "closed",     missionRepository.countByStatus(MissionStatus.CLOSED)
        );
    }

    // ── VALIDATION ────────────────────────────────────────────
    private void validate(MissionDto dto) {
        if (!StringUtils.hasText(dto.getTitle()))
            throw new IllegalArgumentException("Title is required");
        if (!StringUtils.hasText(dto.getEnterpriseName()))
            throw new IllegalArgumentException("Enterprise name is required");
        if (!StringUtils.hasText(dto.getDescription()))
            throw new IllegalArgumentException("Description is required");
    }
}
