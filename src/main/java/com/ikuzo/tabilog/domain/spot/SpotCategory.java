package com.ikuzo.tabilog.domain.spot;

public enum SpotCategory {
    HOTEL("호텔"),
    LANDMARK("랜드마크"),
    SHOPPING("쇼핑"),
    PARK("공원"),
    STREET("거리"),
    RESTAURANT("식당"),
    MUSEUM("박물관"),
    TEMPLE("사원"),
    BEACH("해변"),
    MOUNTAIN("산"),
    OTHER("기타");

    private final String description;

    SpotCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
