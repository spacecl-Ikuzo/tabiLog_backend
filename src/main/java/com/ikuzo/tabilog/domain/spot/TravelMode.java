package com.ikuzo.tabilog.domain.spot;

public enum TravelMode {
    WALKING("도보"),
    TRANSIT("대중교통"),
    DRIVING("자동차"),
    TAXI("택시"),
    BICYCLE("자전거"),
    OTHER("기타");

    private final String description;

    TravelMode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
