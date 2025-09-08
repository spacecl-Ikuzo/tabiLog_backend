package com.ikuzo.tabilog.dto.response;

import lombok.Data;

@Data
public class FindIdResponse {
    
    private String nickname;
    private String message;
    
    public FindIdResponse(String nickname) {
        this.nickname = nickname;
        this.message = "아이디를 찾았습니다.";
    }
}
