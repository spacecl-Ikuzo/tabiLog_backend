package com.ikuzo.tabilog.domain.plan;

import com.ikuzo.tabilog.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "plan_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlanMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanMemberRole role = PlanMemberRole.MEMBER; // 기본값: MEMBER

    @Builder
    public PlanMember(Plan plan, User user, PlanMemberRole role) {
        this.plan = plan;
        this.user = user;
        this.role = role != null ? role : PlanMemberRole.MEMBER;
    }

    // 역할 변경 메서드
    public void changeRole(PlanMemberRole role) {
        this.role = role;
    }

    // 연관관계 편의 메서드
    public void setPlan(Plan plan) {
        this.plan = plan;
        if (plan != null) {
            plan.getPlanMembers().add(this);
        }
    }

    public void setUser(User user) {
        this.user = user;
    }
}
