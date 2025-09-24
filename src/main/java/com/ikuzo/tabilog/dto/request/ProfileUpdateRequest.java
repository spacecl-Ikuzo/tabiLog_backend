package com.ikuzo.tabilog.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileUpdateRequest {
    
    @NotBlank(message = "姓を入力してください")
    @Size(max = 50, message = "姓は50文字以内で入力してください")
    private String lastName;
    
    @NotBlank(message = "名を入力してください")
    @Size(max = 50, message = "名は50文字以内で入力してください")
    private String firstName;
    
    @NotBlank(message = "ニックネームを入力してください")
    @Size(max = 30, message = "ニックネームは30文字以内で入力してください")
    private String nickname;
    
    @NotBlank(message = "メールアドレスを入力してください")
    @Email(message = "有効なメールアドレスを入力してください")
    private String email;
    
    @NotBlank(message = "電話番号を入力してください")
    @Size(max = 20, message = "電話番号は20文字以内で入力してください")
    private String phoneNumber;
    
}

