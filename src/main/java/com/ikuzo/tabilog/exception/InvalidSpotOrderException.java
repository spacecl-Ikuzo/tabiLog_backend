package com.ikuzo.tabilog.exception;

public class InvalidSpotOrderException extends RuntimeException {
    public InvalidSpotOrderException(String message) {
        super(message);
    }

    public InvalidSpotOrderException(Integer visitOrder) {
        super("유효하지 않은 관광지 순서입니다: " + visitOrder);
    }
}
