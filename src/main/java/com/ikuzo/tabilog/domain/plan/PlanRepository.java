package com.ikuzo.tabilog.domain.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    
    // 사용자의 모든 여행 계획 조회
    List<Plan> findAllByUserIdOrderByStartDateDesc(Long userId);
    
    // 사용자의 특정 여행 계획 조회
    Optional<Plan> findByIdAndUserId(Long id, Long userId);
    
    // 사용자의 활성 여행 계획 조회 (현재 날짜 기준)
    @Query("SELECT p FROM Plan p WHERE p.user.id = :userId AND p.startDate <= CURRENT_DATE AND p.endDate >= CURRENT_DATE ORDER BY p.startDate ASC")
    List<Plan> findActivePlansByUserId(@Param("userId") Long userId);
    
    // 사용자의 예정된 여행 계획 조회
    @Query("SELECT p FROM Plan p WHERE p.user.id = :userId AND p.startDate > CURRENT_DATE ORDER BY p.startDate ASC")
    List<Plan> findUpcomingPlansByUserId(@Param("userId") Long userId);
    
    // 사용자의 완료된 여행 계획 조회
    @Query("SELECT p FROM Plan p WHERE p.user.id = :userId AND p.endDate < CURRENT_DATE ORDER BY p.endDate DESC")
    List<Plan> findCompletedPlansByUserId(@Param("userId") Long userId);
}
