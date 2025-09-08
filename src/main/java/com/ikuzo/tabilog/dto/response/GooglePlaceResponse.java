package com.ikuzo.tabilog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GooglePlaceResponse {
    private String placeId;
    private String name;
    private String formattedAddress;
    private String vicinity;
    private Double latitude;
    private Double longitude;
    private String[] types;
    private Double rating;
    private Integer userRatingsTotal;
    private String priceLevel;
    private String photoReference;
    private String businessStatus;
}
