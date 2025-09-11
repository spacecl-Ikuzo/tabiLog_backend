package com.ikuzo.tabilog.domain.plan;

public enum PlanMemberRole {
    OWNER("계획 소유자"),
    EDITOR("계획 편집자"),
    VIEWER("계획 뷰어");

    private final String description;

    PlanMemberRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
