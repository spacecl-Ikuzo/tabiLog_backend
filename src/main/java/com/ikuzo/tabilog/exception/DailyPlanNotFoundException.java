package com.ikuzo.tabilog.exception;

public class DailyPlanNotFoundException extends RuntimeException {
    public DailyPlanNotFoundException(String message) {
        super(message);
    }

    public DailyPlanNotFoundException(Long dailyPlanId) {
        super("일별 계획을 찾을 수 없습니다. ID: " + dailyPlanId);
    }
}
