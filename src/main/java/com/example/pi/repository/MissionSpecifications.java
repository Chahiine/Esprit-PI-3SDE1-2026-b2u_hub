package com.example.pi.repository;

import com.example.pi.entity.Mission;
import com.example.pi.entity.MissionStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class MissionSpecifications {

    private MissionSpecifications() {
    }

    public static Specification<Mission> withFilters(
            String keyword,
            String location,
            MissionStatus status,
            String skills,
            String companyName) {

        return (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

            if (StringUtils.hasText(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("companyName")), pattern),
                        cb.like(cb.lower(root.get("skillsRequired")), pattern)
                ));
            }

            if (StringUtils.hasText(location)) {
                predicates.add(cb.like(
                        cb.lower(root.get("location")),
                        "%" + location.trim().toLowerCase() + "%"
                ));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(skills)) {
                predicates.add(cb.like(
                        cb.lower(root.get("skillsRequired")),
                        "%" + skills.trim().toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(companyName)) {
                predicates.add(cb.like(
                        cb.lower(root.get("companyName")),
                        "%" + companyName.trim().toLowerCase() + "%"
                ));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
