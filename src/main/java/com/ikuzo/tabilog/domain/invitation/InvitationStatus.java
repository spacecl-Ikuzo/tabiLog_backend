package com.ikuzo.tabilog.domain.invitation;

public enum InvitationStatus {
    PENDING("대기중"),
    ACCEPTED("수락됨"),
    REJECTED("거절됨"),
    EXPIRED("만료됨");

    private final String description;

    InvitationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
