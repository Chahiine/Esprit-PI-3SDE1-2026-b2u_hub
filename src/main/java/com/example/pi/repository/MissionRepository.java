package com.example.pi.repository;

import com.example.pi.entity.Mission;
import com.example.pi.entity.Mission.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    List<Mission> findByStatus(MissionStatus status);

    List<Mission> findByCreatedByUserId(Long userId);

    List<Mission> findByCategory(String category);

    @Query("SELECT m FROM Mission m WHERE " +
           "LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.skillsRequired) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.enterpriseName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Mission> search(@Param("keyword") String keyword);

    long countByStatus(MissionStatus status);
}
