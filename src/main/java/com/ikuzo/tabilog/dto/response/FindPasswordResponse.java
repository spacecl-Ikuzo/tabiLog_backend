package com.ikuzo.tabilog.dto.response;

import lombok.Data;

@Data
public class FindPasswordResponse {
    
    private String message;
    
    public FindPasswordResponse() {
        this.message = "비밀번호 재설정 이메일을 발송했습니다.";
    }
}
