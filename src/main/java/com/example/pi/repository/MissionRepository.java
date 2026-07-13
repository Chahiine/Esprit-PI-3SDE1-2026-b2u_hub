package com.example.pi.repository;

import com.example.pi.entity.Mission;
import com.example.pi.entity.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MissionRepository extends JpaRepository<Mission, Long>, JpaSpecificationExecutor<Mission> {

    List<Mission> findByStatus(MissionStatus status);
}
