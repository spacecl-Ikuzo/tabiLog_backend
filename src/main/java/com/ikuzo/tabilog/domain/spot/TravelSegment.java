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
@Table(name = "travel_segment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelSegment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_spot_id", nullable = false)
    private Spot fromSpot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_spot_id", nullable = false)
    private Spot toSpot;

    @Column(nullable = false)
    private String duration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TravelMode travelMode;

    @Column(nullable = false)
    private Integer segmentOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_plan_id", nullable = false)
    private DailyPlan dailyPlan;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public TravelSegment(Spot fromSpot, Spot toSpot, String duration, 
                        TravelMode travelMode, Integer segmentOrder, DailyPlan dailyPlan) {
        this.fromSpot = fromSpot;
        this.toSpot = toSpot;
        this.duration = duration;
        this.travelMode = travelMode;
        this.segmentOrder = segmentOrder;
        this.dailyPlan = dailyPlan;
    }

    // 비즈니스 로직
    public void updateTravelSegment(String duration, TravelMode travelMode) {
        if (duration != null) {
            this.duration = duration;
        }
        if (travelMode != null) {
            this.travelMode = travelMode;
        }
    }

    public void updateSegmentOrder(Integer segmentOrder) {
        this.segmentOrder = segmentOrder;
    }
}
