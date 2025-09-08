package com.ikuzo.tabilog.domain.spot;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelSegmentRepository extends JpaRepository<TravelSegment, Long> {
    
    // 일별 계획의 모든 이동 구간 조회 (순서대로)
    List<TravelSegment> findAllByDailyPlanIdOrderBySegmentOrderAsc(Long dailyPlanId);
    
    // 특정 관광지에서 출발하는 이동 구간 조회
    List<TravelSegment> findAllByFromSpotId(Long fromSpotId);
    
    // 특정 관광지로 도착하는 이동 구간 조회
    List<TravelSegment> findAllByToSpotId(Long toSpotId);
    
    // 두 관광지 간의 이동 구간 조회
    Optional<TravelSegment> findByFromSpotIdAndToSpotId(Long fromSpotId, Long toSpotId);
    
    // 일별 계획의 특정 순서 이후 이동 구간들 조회
    @Query("SELECT ts FROM TravelSegment ts WHERE ts.dailyPlan.id = :dailyPlanId AND ts.segmentOrder > :segmentOrder ORDER BY ts.segmentOrder ASC")
    List<TravelSegment> findSegmentsAfterOrder(@Param("dailyPlanId") Long dailyPlanId, @Param("segmentOrder") Integer segmentOrder);
    
    // 일별 계획의 최대 이동 구간 순서 조회
    @Query("SELECT COALESCE(MAX(ts.segmentOrder), 0) FROM TravelSegment ts WHERE ts.dailyPlan.id = :dailyPlanId")
    Integer findMaxSegmentOrderByDailyPlanId(@Param("dailyPlanId") Long dailyPlanId);
}
