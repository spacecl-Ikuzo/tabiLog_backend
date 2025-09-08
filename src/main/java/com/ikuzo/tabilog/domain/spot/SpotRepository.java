package com.ikuzo.tabilog.domain.spot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {
    
    // 일별 계획의 모든 관광지 조회 (방문 순서대로)
    List<Spot> findAllByDailyPlanIdOrderByVisitOrderAsc(Long dailyPlanId);
    
    // 특정 일별 계획의 관광지 조회
    Optional<Spot> findByIdAndDailyPlanId(Long id, Long dailyPlanId);
    
    // 관광지 이름으로 검색
    @Query("SELECT s FROM Spot s WHERE s.name LIKE %:name% ORDER BY s.name ASC")
    List<Spot> findByNameContaining(@Param("name") String name);
    
    // 카테고리별 관광지 조회
    List<Spot> findAllByCategoryOrderByNameAsc(SpotCategory category);
    
    // 일별 계획의 특정 순서 이후 관광지들 조회
    @Query("SELECT s FROM Spot s WHERE s.dailyPlan.id = :dailyPlanId AND s.visitOrder > :visitOrder ORDER BY s.visitOrder ASC")
    List<Spot> findSpotsAfterOrder(@Param("dailyPlanId") Long dailyPlanId, @Param("visitOrder") Integer visitOrder);
    
    // 일별 계획의 최대 방문 순서 조회
    @Query("SELECT COALESCE(MAX(s.visitOrder), 0) FROM Spot s WHERE s.dailyPlan.id = :dailyPlanId")
    Integer findMaxVisitOrderByDailyPlanId(@Param("dailyPlanId") Long dailyPlanId);
}
