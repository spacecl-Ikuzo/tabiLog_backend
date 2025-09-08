package com.ikuzo.tabilog.domain.spot;

import com.ikuzo.tabilog.domain.plan.DailyPlan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "spot")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpotCategory category;

    @Column(nullable = false)
    private Integer visitOrder;

    @Column(nullable = false)
    private String duration;

    @Column(nullable = false)
    private Long cost = 0L;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_plan_id", nullable = false)
    private DailyPlan dailyPlan;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Spot(String name, String address, SpotCategory category, Integer visitOrder, 
                String duration, Long cost, Double latitude, Double longitude, DailyPlan dailyPlan) {
        this.name = name;
        this.address = address;
        this.category = category;
        this.visitOrder = visitOrder;
        this.duration = duration;
        this.cost = cost;
        this.latitude = latitude;
        this.longitude = longitude;
        this.dailyPlan = dailyPlan;
    }

    // 비즈니스 로직
    public void updateSpot(String name, String address, SpotCategory category, 
                          String duration, Long cost, Double latitude, Double longitude) {
        if (name != null) {
            this.name = name;
        }
        if (address != null) {
            this.address = address;
        }
        if (category != null) {
            this.category = category;
        }
        if (duration != null) {
            this.duration = duration;
        }
        if (cost != null) {
            this.cost = cost;
        }
        if (latitude != null) {
            this.latitude = latitude;
        }
        if (longitude != null) {
            this.longitude = longitude;
        }
    }

    public void updateVisitOrder(Integer visitOrder) {
        this.visitOrder = visitOrder;
    }

    public void setDailyPlan(DailyPlan dailyPlan) {
        this.dailyPlan = dailyPlan;
    }
}
