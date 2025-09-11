package com.ikuzo.tabilog.domain.plan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanMemberRepository extends JpaRepository<PlanMember, Long> {
    
    // 특정 계획의 모든 멤버 조회
    List<PlanMember> findByPlanId(Long planId);
    
    // 특정 계획의 특정 역할 멤버 조회
    List<PlanMember> findByPlanIdAndRole(Long planId, PlanMemberRole role);
    
    // 특정 사용자의 모든 계획 멤버 조회
    List<PlanMember> findByUserId(Long userId);
    
    // 특정 사용자의 특정 역할 계획 멤버 조회
    List<PlanMember> findByUserIdAndRole(Long userId, PlanMemberRole role);
    
    // 특정 계획에서 특정 사용자의 멤버 조회
    Optional<PlanMember> findByPlanIdAndUserId(Long planId, Long userId);
    
    // 특정 계획에 특정 사용자가 멤버인지 확인
    boolean existsByPlanIdAndUserId(Long planId, Long userId);
    
    // 특정 계획의 멤버 수 조회
    @Query("SELECT COUNT(pm) FROM PlanMember pm WHERE pm.plan.id = :planId")
    Long countByPlanId(@Param("planId") Long planId);
    
    // 특정 계획의 오너 조회
    @Query("SELECT pm FROM PlanMember pm WHERE pm.plan.id = :planId AND pm.role = 'OWNER'")
    Optional<PlanMember> findOwnerByPlanId(@Param("planId") Long planId);
    
    // 특정 사용자가 오너인 계획들 조회
    @Query("SELECT pm FROM PlanMember pm WHERE pm.user.id = :userId AND pm.role = 'OWNER'")
    List<PlanMember> findOwnedPlansByUserId(@Param("userId") Long userId);
}
