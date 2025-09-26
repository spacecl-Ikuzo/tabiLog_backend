package com.ikuzo.tabilog.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetVerifyCodeRequest {

	@NotBlank(message = "이메일은 필수입니다")
	@Email(message = "올바른 이메일 형식이 아닙니다")
	private String email;

	@NotBlank(message = "인증코드는 필수입니다")
	@Size(min = 6, max = 6, message = "인증코드는 6자리여야 합니다")
	@JsonAlias({"verificationCode","authCode","otp","verifyCode"})
	private String code;
}


