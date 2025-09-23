package com.ikuzo.tabilog.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileImageUpdateRequest {
    
    @NotBlank(message = "프로필 이미지 URL을 입력해주세요")
    private String profileImageUrl;
}

