package com.ikuzo.tabilog.domain.user;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
@ToString 
class UserDTO {

    private Long id;
    private String email;
    private String password;
    private String name;
    private String gender;
    private String phoneNumber;
    private String nickname;  // 유저가 로그인 시 사용하는 ID
    private String profileImageOriginalKey; // 프로필 이미지 원본 키
    private String profileImageThumbKey; // 프로필 이미지 실사용 키
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
