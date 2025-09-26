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
    
    // 사용자의 특정 여행 계획 조회 (DailyPlan 포함)
    @Query("SELECT p FROM Plan p LEFT JOIN FETCH p.dailyPlans dp WHERE p.id = :id AND p.user.id = :userId")
    Optional<Plan> findByIdAndUserIdWithDailyPlans(@Param("id") Long id, @Param("userId") Long userId);
    
    // 사용자가 작성하거나 멤버로 참여한 특정 여행 계획 조회 (DailyPlan 포함)
    @Query("SELECT DISTINCT p FROM Plan p LEFT JOIN FETCH p.dailyPlans dp LEFT JOIN p.planMembers pm WHERE p.id = :id AND (p.user.id = :userId OR pm.user.id = :userId)")
    Optional<Plan> findByIdAndMemberUserIdWithDailyPlans(@Param("id") Long id, @Param("userId") Long userId);
    
    // 사용자의 활성 여행 계획 조회 (현재 날짜 기준)
    @Query("SELECT p FROM Plan p WHERE p.user.id = :userId AND p.startDate <= CURRENT_DATE AND p.endDate >= CURRENT_DATE ORDER BY p.startDate ASC")
    List<Plan> findActivePlansByUserId(@Param("userId") Long userId);
    
    // 사용자의 예정된 여행 계획 조회
    @Query("SELECT p FROM Plan p WHERE p.user.id = :userId AND p.startDate > CURRENT_DATE ORDER BY p.startDate ASC")
    List<Plan> findUpcomingPlansByUserId(@Param("userId") Long userId);
    
    // 사용자의 완료된 여행 계획 조회
    @Query("SELECT p FROM Plan p WHERE p.user.id = :userId AND p.endDate < CURRENT_DATE ORDER BY p.endDate DESC")
    List<Plan> findCompletedPlansByUserId(@Param("userId") Long userId);
    
    // 공개된 모든 여행 계획 조회 (필터링 없음)
    @Query("SELECT p FROM Plan p WHERE p.isPublic = true AND p.user.publicAgreement = true ORDER BY p.createdAt DESC")
    List<Plan> findAllPublicPlans();
    
    // 지역별 공개된 여행 계획 조회
    @Query("SELECT p FROM Plan p WHERE p.isPublic = true AND p.user.publicAgreement = true AND p.region = :region ORDER BY p.createdAt DESC")
    List<Plan> findPublicPlansByRegion(@Param("region") String region);
    
    // 현(prefecture)별 공개된 여행 계획 조회
    @Query("SELECT p FROM Plan p WHERE p.isPublic = true AND p.user.publicAgreement = true AND p.prefecture = :prefecture ORDER BY p.createdAt DESC")
    List<Plan> findPublicPlansByPrefecture(@Param("prefecture") String prefecture);
    
    // 상태별 공개된 여행 계획 조회
    @Query("SELECT p FROM Plan p WHERE p.isPublic = true AND p.user.publicAgreement = true AND p.status = :status ORDER BY p.createdAt DESC")
    List<Plan> findPublicPlansByStatus(@Param("status") String status);
    
    // 지역과 상태로 공개된 여행 계획 조회
    @Query("SELECT p FROM Plan p WHERE p.isPublic = true AND p.user.publicAgreement = true AND p.region = :region AND p.status = :status ORDER BY p.createdAt DESC")
    List<Plan> findPublicPlansByRegionAndStatus(@Param("region") String region, @Param("status") String status);
    
    // 현(prefecture)과 상태로 공개된 여행 계획 조회
    @Query("SELECT p FROM Plan p WHERE p.isPublic = true AND p.user.publicAgreement = true AND p.prefecture = :prefecture AND p.status = :status ORDER BY p.createdAt DESC")
    List<Plan> findPublicPlansByPrefectureAndStatus(@Param("prefecture") String prefecture, @Param("status") String status);
    
    // 지역, 현, 상태로 공개된 여행 계획 조회 (모든 필터)
    @Query("SELECT p FROM Plan p WHERE p.isPublic = true AND p.user.publicAgreement = true AND " +
           "(:region IS NULL OR p.region = :region) AND " +
           "(:prefecture IS NULL OR p.prefecture = :prefecture) AND " +
           "(:status IS NULL OR p.status = :status) " +
           "ORDER BY p.createdAt DESC")
    List<Plan> findPublicPlansWithFilters(@Param("region") String region, 
                                         @Param("prefecture") String prefecture, 
                                         @Param("status") String status);
    
    // 사용자가 작성하거나 멤버로 참여한 모든 여행 계획 조회
    @Query("SELECT DISTINCT p FROM Plan p LEFT JOIN p.planMembers pm WHERE p.user.id = :userId OR pm.user.id = :userId ORDER BY p.startDate DESC")
    List<Plan> findAllByMemberUserIdOrderByStartDateDesc(@Param("userId") Long userId);
    
    // 사용자가 작성하거나 멤버로 참여한 활성 여행 계획 조회
    @Query("SELECT DISTINCT p FROM Plan p LEFT JOIN p.planMembers pm WHERE (p.user.id = :userId OR pm.user.id = :userId) AND p.startDate <= CURRENT_DATE AND p.endDate >= CURRENT_DATE ORDER BY p.startDate ASC")
    List<Plan> findActivePlansByMemberUserId(@Param("userId") Long userId);
    
    // 사용자가 작성하거나 멤버로 참여한 예정된 여행 계획 조회
    @Query("SELECT DISTINCT p FROM Plan p LEFT JOIN p.planMembers pm WHERE (p.user.id = :userId OR pm.user.id = :userId) AND p.startDate > CURRENT_DATE ORDER BY p.startDate ASC")
    List<Plan> findUpcomingPlansByMemberUserId(@Param("userId") Long userId);
    
    // 사용자가 작성하거나 멤버로 참여한 완료된 여행 계획 조회
    @Query("SELECT DISTINCT p FROM Plan p LEFT JOIN p.planMembers pm WHERE (p.user.id = :userId OR pm.user.id = :userId) AND p.endDate < CURRENT_DATE ORDER BY p.endDate DESC")
    List<Plan> findCompletedPlansByMemberUserId(@Param("userId") Long userId);
}
