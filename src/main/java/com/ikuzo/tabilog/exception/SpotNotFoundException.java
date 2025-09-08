package com.ikuzo.tabilog.exception;

public class SpotNotFoundException extends RuntimeException {
    public SpotNotFoundException(String message) {
        super(message);
    }

    public SpotNotFoundException(Long spotId) {
        super("관광지를 찾을 수 없습니다. ID: " + spotId);
    }
}
