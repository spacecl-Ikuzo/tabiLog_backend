package com.ikuzo.tabilog.domain.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {
    List<DailyPlan> findAllByPlanIdOrderByVisitDateAsc(Long planId);
    Optional<DailyPlan> findByPlanIdAndVisitDate(Long planId, LocalDate visitDate);
}
