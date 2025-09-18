package com.ikuzo.tabilog.domain.invitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanInvitationRepository extends JpaRepository<PlanInvitation, Long> {

    // 토큰으로 초대 찾기
    Optional<PlanInvitation> findByToken(String token);

    // 플랜과 이메일로 초대 찾기 (중복 초대 방지)
    Optional<PlanInvitation> findByPlanIdAndInviteeEmail(Long planId, String inviteeEmail);

    // 특정 플랜의 모든 초대 조회
    List<PlanInvitation> findByPlanIdOrderByCreatedAtDesc(Long planId);

    // 특정 이메일의 모든 초대 조회
    List<PlanInvitation> findByInviteeEmailOrderByCreatedAtDesc(String inviteeEmail);

    // 대기 중인 초대만 조회
    @Query("SELECT pi FROM PlanInvitation pi WHERE pi.inviteeEmail = :email AND pi.status = 'PENDING' AND pi.expiresAt > CURRENT_TIMESTAMP")
    List<PlanInvitation> findPendingInvitationsByEmail(@Param("email") String email);

    // 만료된 초대 조회
    @Query("SELECT pi FROM PlanInvitation pi WHERE pi.status = 'PENDING' AND pi.expiresAt <= CURRENT_TIMESTAMP")
    List<PlanInvitation> findExpiredInvitations();
}
