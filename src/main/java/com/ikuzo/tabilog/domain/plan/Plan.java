package com.ikuzo.tabilog.domain.plan;

import com.ikuzo.tabilog.domain.user.User;
import com.ikuzo.tabilog.domain.expense.Expense;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String region; //동서남북 일본

    @Column(nullable = false)
    private String prefecture; //도

    @Column
    private String prefectureImageUrl; //현별 배경 이미지 URL

    @Column(nullable = false)
    private Long participant_count; //인원수

    @Column(nullable = false)
    private Long totalBudget;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private boolean isPublic; //공개여부

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("visitDate ASC")
    private List<DailyPlan> dailyPlans = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanMember> planMembers = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("expenseDate ASC")
    private List<Expense> expenses = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Plan(String title, LocalDate startDate, LocalDate endDate, Long totalBudget, User user, String region, String status, String prefecture, String prefectureImageUrl, Long participant_count, boolean isPublic) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.region = region;
        this.prefecture = prefecture;
        this.prefectureImageUrl = prefectureImageUrl;
        this.participant_count = participant_count;
        this.totalBudget = totalBudget;
        this.status = status;
        this.isPublic = isPublic;
        this.user = user;
    }

    // 비즈니스 로직
    public void updatePlan(String title, LocalDate startDate, LocalDate endDate, Long totalBudget) {
        if (title != null) {
            this.title = title;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
        if (totalBudget != null) {
            this.totalBudget = totalBudget;
        }
    }

    public void addDailyPlan(DailyPlan dailyPlan) {
        this.dailyPlans.add(dailyPlan);
    }

    public void removeDailyPlan(DailyPlan dailyPlan) {
        this.dailyPlans.remove(dailyPlan);
    }

    // PlanMember 관련 메서드
    public void addMember(User user, PlanMemberRole role) {
        PlanMember planMember = PlanMember.builder()
                .plan(this)
                .user(user)
                .role(role)
                .build();
        this.planMembers.add(planMember);
    }

    public void removeMember(User user) {
        this.planMembers.removeIf(member -> member.getUser().getId().equals(user.getId()));
    }

    public boolean isMember(User user) {
        return this.planMembers.stream()
                .anyMatch(member -> member.getUser().getId().equals(user.getId()));
    }

    public PlanMemberRole getMemberRole(User user) {
        return this.planMembers.stream()
                .filter(member -> member.getUser().getId().equals(user.getId()))
                .map(PlanMember::getRole)
                .findFirst()
                .orElse(null);
    }

    public List<User> getMembers() {
        return this.planMembers.stream()
                .map(PlanMember::getUser)
                .toList();
    }

    public List<User> getMembersByRole(PlanMemberRole role) {
        return this.planMembers.stream()
                .filter(member -> member.getRole() == role)
                .map(PlanMember::getUser)
                .toList();
    }

    // Expense 관련 메서드
    public void addExpense(Expense expense) {
        this.expenses.add(expense);
    }

    public void removeExpense(Expense expense) {
        this.expenses.remove(expense);
    }

    // 총 지출 금액 계산
    public Long getTotalExpenseAmount() {
        return this.expenses.stream()
                .mapToLong(expense -> expense.getAmount().longValue())
                .sum();
    }
}
