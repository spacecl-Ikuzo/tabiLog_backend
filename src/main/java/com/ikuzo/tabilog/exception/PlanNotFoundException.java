package com.ikuzo.tabilog.exception;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException(String message) {
        super(message);
    }

    public PlanNotFoundException(Long planId) {
        super("여행 계획을 찾을 수 없습니다. ID: " + planId);
    }
}
