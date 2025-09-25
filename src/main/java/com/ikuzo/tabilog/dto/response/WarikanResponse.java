package com.ikuzo.tabilog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarikanResponse {

    private Long id;
    private Long planId;
    private String title;
    private Long totalAmount;
    private String senderName;
    private String senderEmail;
    private List<MemberShareResponse> memberShares;
    private LocalDateTime createdAt;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberShareResponse {
        private Long userId;
        private String memberName;
        private String memberEmail;
        private Long amount;
        private String profileImageUrl;
    }
}
