package com.ikuzo.tabilog.dto.response;

import lombok.Data;

@Data
public class FindIdResponse {
    
    private String userId;
    private String message;
    
    public FindIdResponse(String userId) {
        this.userId = userId;
        this.message = "아이디를 찾았습니다.";
    }
}
