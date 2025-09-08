package com.ikuzo.tabilog.dto.request;

import com.ikuzo.tabilog.domain.spot.SpotCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SpotRequest {

    @NotBlank(message = "관광지 이름은 필수입니다")
    private String name;

    @NotBlank(message = "주소는 필수입니다")
    private String address;

    @NotNull(message = "카테고리는 필수입니다")
    private SpotCategory category;

    @NotNull(message = "방문 순서는 필수입니다")
    @PositiveOrZero(message = "방문 순서는 0 이상이어야 합니다")
    private Integer visitOrder;

    @NotBlank(message = "체류 시간은 필수입니다")
    private String duration;

    @NotNull(message = "비용은 필수입니다")
    @PositiveOrZero(message = "비용은 0 이상이어야 합니다")
    private Long cost;

    private Double latitude;
    private Double longitude;
}
