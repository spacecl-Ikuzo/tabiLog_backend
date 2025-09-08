package com.ikuzo.tabilog.dto.response;

import com.ikuzo.tabilog.domain.spot.TravelMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelSegmentResponse {
    private Long id;
    private Long fromSpotId;
    private String fromSpotName;
    private Long toSpotId;
    private String toSpotName;
    private String duration;
    private TravelMode travelMode;
    private Integer segmentOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
