package com.ikuzo.tabilog.domain.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyPlanRepository extends JpaRepository<DailyPlan, Long> {
    List<DailyPlan> findAllByPlanIdOrderByVisitDateAsc(Long planId);
    Optional<DailyPlan> findByPlanIdAndVisitDate(Long planId, LocalDate visitDate);
    List<DailyPlan> findByPlanAndVisitDate(Plan plan, LocalDate visitDate);

    // DailyPlan과 spots를 함께 가져오기
    @Query("SELECT dp FROM DailyPlan dp LEFT JOIN FETCH dp.spots s WHERE dp.plan.id = :planId ORDER BY dp.visitDate ASC")
    List<DailyPlan> findAllByPlanIdWithSpots(@Param("planId") Long planId);

    // DailyPlan과 travelSegments를 함께 가져오기
    @Query("SELECT dp FROM DailyPlan dp LEFT JOIN FETCH dp.travelSegments ts WHERE dp.plan.id = :planId ORDER BY dp.visitDate ASC")
    List<DailyPlan> findAllByPlanIdWithTravelSegments(@Param("planId") Long planId);
}
