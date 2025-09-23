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
@Table(
        name = "user", // 기존 테이블명 유지
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_userid", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_user_nickname", columnNames = "nickname")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ⚠️ column 이름을 명시해서 위 uniqueConstraints와 정확히 매칭되게 함
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // 로그인 ID
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String gender;
    private String phoneNumber;

    @Column(name = "nickname", nullable = false, unique = true)
    private String nickname;

    // 프로필 이미지 관련 필드
    @Column(name = "profile_image_url")
    private String profileImageUrl; // 프로필 이미지 URL

    @Column(nullable = false)
    private Boolean privacyAgreement; // 개인정보동의서 (필수)

    private Boolean publicAgreement; // 공개동의서 (선택)

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.ikuzo.tabilog.domain.plan.PlanMember> planMembers = new ArrayList<>();

    @Builder
    public User(String email, String userId, String password, String firstName, String lastName,
                String gender, String phoneNumber, String nickname,
                String profileImageUrl,
                Boolean privacyAgreement, Boolean publicAgreement) {
        this.email = email;
        this.userId = userId;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.privacyAgreement = privacyAgreement;
        this.publicAgreement = publicAgreement;
    }

    //== 비즈니스 로직 ==//
    public void updateProfile(String firstName, String lastName, String nickname, String phoneNumber) {
        if (firstName != null) this.firstName = firstName;
        if (lastName != null) this.lastName = lastName;
        if (nickname != null) this.nickname = nickname;
        if (phoneNumber != null) this.phoneNumber = phoneNumber;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

    public void updateEmail(String email) {
        this.email = email;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
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
