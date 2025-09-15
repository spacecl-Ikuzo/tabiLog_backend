package com.ikuzo.tabilog.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "user") // DBML의 'User' 테이블과 매핑됩니다.
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA는 기본 생성자를 필요로 합니다.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String userId; // 사용자가 로그인할 때 사용하는 ID

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;

    private String gender;
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String nickname;
    
    @Column(nullable = false)
    private Boolean privacyAgreement; // 개인정보동의서 (필수)
    
    private Boolean publicAgreement; // 공개동의서 (선택)
    
    // DBML 스키마에 따라 나머지 필드들도 추가합니다...
    // private String profileImageOriginalKey;
    // private String profileImageThumbKey;

    @CreationTimestamp // 엔티티가 생성될 때 자동으로 시간이 기록됩니다.
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티가 수정될 때 자동으로 시간이 기록됩니다.
    private LocalDateTime updatedAt;

    // PlanMember 관계 (User가 참여하는 Plan들)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.ikuzo.tabilog.domain.plan.PlanMember> planMembers = new ArrayList<>();

    @Builder
    public User(String email, String userId, String password, String firstName, String lastName, String gender, String phoneNumber, String nickname, Boolean privacyAgreement, Boolean publicAgreement) {
        this.email = email;
        this.userId = userId;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.privacyAgreement = privacyAgreement;
        this.publicAgreement = publicAgreement;
    }

    //== 비즈니스 로직 ==//
    public void updateProfile(String nickname, String phoneNumber) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        if (phoneNumber != null) {
            this.phoneNumber = phoneNumber;
        }
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    //== PlanMember 관련 편의 메서드 ==//
    public List<com.ikuzo.tabilog.domain.plan.Plan> getParticipatingPlans() {
        return this.planMembers.stream()
                .map(com.ikuzo.tabilog.domain.plan.PlanMember::getPlan)
                .toList();
    }

    public List<com.ikuzo.tabilog.domain.plan.Plan> getOwnedPlans() {
        return this.planMembers.stream()
                .filter(member -> member.getRole() == com.ikuzo.tabilog.domain.plan.PlanMemberRole.OWNER)
                .map(com.ikuzo.tabilog.domain.plan.PlanMember::getPlan)
                .toList();
    }
}
