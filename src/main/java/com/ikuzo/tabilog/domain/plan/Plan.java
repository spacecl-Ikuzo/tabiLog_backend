package com.ikuzo.tabilog.domain.plan;

import com.ikuzo.tabilog.domain.user.User;
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
    private Long totalBudget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("visitDate ASC")
    private List<DailyPlan> dailyPlans = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Plan(String title, LocalDate startDate, LocalDate endDate, Long totalBudget, User user) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalBudget = totalBudget;
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
}
