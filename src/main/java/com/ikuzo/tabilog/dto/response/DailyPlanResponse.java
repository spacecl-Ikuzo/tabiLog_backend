package com.ikuzo.tabilog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyPlanResponse {
    private Long id;
    private LocalDate visitDate;
    private LocalTime departureTime;
    private List<SpotResponse> spots;
    private List<TravelSegmentResponse> travelSegments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
