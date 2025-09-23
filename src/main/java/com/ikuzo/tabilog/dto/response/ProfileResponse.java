package com.ikuzo.tabilog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponse {
    private Long id;
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String nickname;
    private String phoneNumber;
    private String gender;
    private String originalProfileImageUrl;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

