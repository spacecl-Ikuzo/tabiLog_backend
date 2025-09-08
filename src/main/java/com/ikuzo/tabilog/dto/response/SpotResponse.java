package com.ikuzo.tabilog.dto.response;

import com.ikuzo.tabilog.domain.spot.SpotCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotResponse {
    private Long id;
    private String name;
    private String address;
    private SpotCategory category;
    private Integer visitOrder;
    private String duration;
    private Long cost;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
