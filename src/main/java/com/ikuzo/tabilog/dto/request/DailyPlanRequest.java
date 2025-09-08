package com.ikuzo.tabilog.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyPlanRequest {

    @NotNull(message = "방문 날짜는 필수입니다")
    private LocalDate visitDate;

    @NotNull(message = "출발 시간은 필수입니다")
    private LocalTime departureTime;

    private List<SpotRequest> spots;
}
