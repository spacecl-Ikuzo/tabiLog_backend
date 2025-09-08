package com.ikuzo.tabilog.dto.request;

import com.ikuzo.tabilog.domain.spot.TravelMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TravelSegmentRequest {

    @NotNull(message = "출발 관광지 ID는 필수입니다")
    private Long fromSpotId;

    @NotNull(message = "도착 관광지 ID는 필수입니다")
    private Long toSpotId;

    @NotBlank(message = "이동 시간은 필수입니다")
    private String duration;

    @NotNull(message = "이동 수단은 필수입니다")
    private TravelMode travelMode;

    @NotNull(message = "구간 순서는 필수입니다")
    @PositiveOrZero(message = "구간 순서는 0 이상이어야 합니다")
    private Integer segmentOrder;
}
