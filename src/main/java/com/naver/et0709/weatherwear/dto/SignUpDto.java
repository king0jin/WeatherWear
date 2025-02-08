package com.naver.et0709.weatherwear.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignUpDto {
    @NotBlank(message = "이름을 입력해주세요.")
    private String name;
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
    @NotBlank(message = "인증 번호를 입력해주세요.")
    private String verificationCode;
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String pw;
    @NotBlank(message = "비밀번호를 확인해주세요.")
    private String confirm_pw;
}
