package com.ikuzo.tabilog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyPageResponse {
    private Long id;
    private String email;
    private String userId;
    private String firstName;
    private String lastName;
    private String gender;
    private String phoneNumber;
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    // 참여 중인 플랜 수
    private int participatingPlanCount;
    // 소유한 플랜 수
    private int ownedPlanCount;
}