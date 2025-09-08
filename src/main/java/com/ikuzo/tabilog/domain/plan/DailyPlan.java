package com.ikuzo.tabilog.domain.plan;

import com.ikuzo.tabilog.domain.spot.Spot;
import com.ikuzo.tabilog.domain.spot.TravelSegment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_plan", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"plan_id", "visit_date"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = false)
    private LocalTime departureTime = LocalTime.of(9, 0);

    @OneToMany(mappedBy = "dailyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("visitOrder ASC")
    private List<Spot> spots = new ArrayList<>();

    @OneToMany(mappedBy = "dailyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("segmentOrder ASC")
    private List<TravelSegment> travelSegments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public DailyPlan(Plan plan, LocalDate visitDate, LocalTime departureTime) {
        this.plan = plan;
        this.visitDate = visitDate;
        this.departureTime = departureTime;
    }
    
    public void updateDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public void addSpot(Spot spot) {
        this.spots.add(spot);
        spot.setDailyPlan(this);
    }

    public void removeSpot(Spot spot) {
        this.spots.remove(spot);
        spot.setDailyPlan(null);
    }

    public void addTravelSegment(TravelSegment travelSegment) {
        this.travelSegments.add(travelSegment);
    }

    public void removeTravelSegment(TravelSegment travelSegment) {
        this.travelSegments.remove(travelSegment);
    }
}