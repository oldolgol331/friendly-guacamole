package com.example.demo.domain.account.dto;

import static com.example.demo.domain.account.constant.AccountConst.EMAIL_REGEX;
import static com.example.demo.domain.account.constant.AccountConst.NICKNAME_REGEX;
import static com.example.demo.domain.account.constant.AccountConst.PASSWORD_REGEX;
import static lombok.AccessLevel.PRIVATE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * PackageName : com.example.demo.domain.account.dto
 * FileName    : AccountRequest
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 계정 도메인 요청 DTO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
@Schema(name = "계정 도메인 요청 DTO")
public abstract class AccountRequest {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "로그인 요청 DTO")
    public static class AccountSignInRequest {

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효하지 않은 이메일 형식입니다.")
        @Schema(name = "이메일(아이디)")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Schema(name = "비밀번호")
        private String password;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "회원 가입 요청 DTO")
    public static class AccountSignUpRequest {

        @NotBlank(message = "이메일은 필수입니다.")
        @Pattern(regexp = EMAIL_REGEX, message = "유효하지 않은 이메일 형식입니다.")
        //@Email(message = "유효하지 않은 이메일 형식입니다.")
        @Schema(name = "이메일 주소(아이디)")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = PASSWORD_REGEX, message = "비밀번호는 8~20자 영문 대/소문자, 숫자, 특수문자를 반드시 1개씩 포함해서 사용하세요.")
        @Schema(name = "비밀번호")
        private String password;

        @NotBlank(message = "비밀번호 확인은 필수입니다.")
        @Schema(name = "비밀번호 확인")
        private String confirmPassword;

        @NotBlank(message = "닉네임은 필수입니다.")
        @Pattern(regexp = NICKNAME_REGEX, message = "닉네임은 2~15자 영문, 한글, 숫자, '-', '_'만 사용 가능합니다.")
        @Schema(name = "닉네임")
        private String nickname;

        @AssertTrue(message = "비밀번호와 비밀번호 확인이 일치하지 않습니다.")
        public boolean isPasswordConfirmed() {
            if (password == null || confirmPassword == null) return true;
            return password.equals(confirmPassword);
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "계정 정보 수정 요청 DTO")
    public static class AccountUpdateRequest {

        @NotBlank(message = "새 닉네임은 필수입니다.")
        @Pattern(regexp = NICKNAME_REGEX, message = "닉네임은 2~15자 영문, 한글, 숫자, '-', '_'만 사용 가능합니다.")
        @Schema(name = "수정할 닉네임")
        private String newNickname;

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        @Schema(name = "현재 비밀번호")
        private String currentPassword;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "계정 비밀번호 변경 요청 DTO")
    public static class AccountPasswordUpdateRequest {

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(regexp = PASSWORD_REGEX, message = "비밀번호는 8~20자 영문 대/소문자, 숫자, 특수문자를 반드시 1개씩 포함해서 사용하세요.")
        @Schema(name = "새 비밀번호")
        private String newPassword;

        @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
        @Schema(name = "새 비밀번호 확인")
        private String confirmNewPassword;

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        @Schema(name = "현재 비밀번호")
        private String currentPassword;

        @AssertTrue(message = "새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.")
        public boolean isNewPasswordConfirmed() {
            return StringUtils.hasText(newPassword) && newPassword.equals(confirmNewPassword);
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "인증 메일 재전송 요청 DTO")
    public static class ResendVerificationEmailRequest {

        @NotBlank(message = "이메일은 필수입니다.")
        @Pattern(regexp = EMAIL_REGEX, message = "유효하지 않은 이메일 형식입니다.")
        @Schema(name = "이메일 주소")
        private String email;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "비밀번호 재설정 요청 DTO")
    public static class PasswordResetRequest {

        @NotBlank(message = "이메일은 필수입니다.")
        @Pattern(regexp = EMAIL_REGEX, message = "유효하지 않은 이메일 형식입니다.")
        @Schema(name = "이메일 주소")
        private String email;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "비밀번호 재설정 확인 요청 DTO")
    public static class PasswordResetConfirmRequest {

        @NotBlank(message = "새 비밀번호는 필수입니다.")
        @Pattern(regexp = PASSWORD_REGEX, message = "비밀번호는 8~20자 영문 대/소문자, 숫자, 특수문자를 반드시 1개씩 포함해서 사용하세요.")
        @Schema(name = "새 비밀번호")
        private String newPassword;

        @NotBlank(message = "새 비밀번호 확인은 필수입니다.")
        @Schema(name = "새 비밀번호 확인")
        private String confirmNewPassword;

        @AssertTrue(message = "새 비밀번호와 새 비밀번호 확인이 일치하지 않습니다.")
        public boolean isNewPasswordConfirmed() {
            return StringUtils.hasText(newPassword) && newPassword.equals(confirmNewPassword);
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "회원 탈퇴 요청 DTO")
    public static class AccountWithdrawRequest {

        @NotBlank(message = "현재 비밀번호는 필수입니다.")
        @Schema(name = "현재 비밀번호")
        private String currentPassword;

    }

}
