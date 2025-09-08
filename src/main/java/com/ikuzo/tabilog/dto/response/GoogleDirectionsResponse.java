package com.ikuzo.tabilog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleDirectionsResponse {
    private String status;
    private List<Route> routes;
    private String errorMessage;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Route {
        private String summary;
        private List<Leg> legs;
        private String overviewPolyline;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Leg {
        private Distance distance;
        private Duration duration;
        private String startAddress;
        private String endAddress;
        private List<Step> steps;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Distance {
        private String text;
        private Long value; // 미터 단위
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Duration {
        private String text;
        private Long value; // 초 단위
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Step {
        private Distance distance;
        private Duration duration;
        private String htmlInstructions;
        private String travelMode;
    }
}
