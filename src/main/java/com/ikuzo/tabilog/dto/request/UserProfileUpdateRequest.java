package com.ikuzo.tabilog.dto.request;

import lombok.Data;
import jakarta.validation.constraints.Size;

@Data
public class UserProfileUpdateRequest {
    
    @Size(max = 50, message = "닉네임은 50자를 초과할 수 없습니다")
    private String nickname;
    
    private String phoneNumber;
}
