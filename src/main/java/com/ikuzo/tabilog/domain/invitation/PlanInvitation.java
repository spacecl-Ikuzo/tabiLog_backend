package com.ikuzo.tabilog.domain.invitation;

import com.ikuzo.tabilog.domain.plan.Plan;
import com.ikuzo.tabilog.domain.plan.PlanMemberRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_invitation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private String inviteeEmail;

    @Column(nullable = false, unique = true)
    private String token; // 초대 토큰

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status = InvitationStatus.PENDING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public PlanInvitation(Plan plan, String inviteeEmail, String token, PlanMemberRole role, LocalDateTime expiresAt) {
        this.plan = plan;
        this.inviteeEmail = inviteeEmail;
        this.token = token;
        this.role = role;
        this.expiresAt = expiresAt;
    }

    // 초대 수락
    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
    }

    // 초대 거절
    public void reject() {
        this.status = InvitationStatus.REJECTED;
    }

    // 만료 여부 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // 유효성 확인 (만료되지 않고 대기 상태인 경우)
    public boolean isValid() {
        return status == InvitationStatus.PENDING && !isExpired();
    }
}
