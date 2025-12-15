package com.example.demo.common.response;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static lombok.AccessLevel.PRIVATE;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * PackageName : com.example.demo.common.response
 * FileName    : ApiResponse
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 공통 API 응답
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Getter
@RequiredArgsConstructor(access = PRIVATE)
public class ApiResponse<T> {

    @JsonIgnore
    private final HttpStatus status;
    @NotBlank
    private final String     message;
    @JsonInclude(NON_NULL)
    private final T          data;

    private ApiResponse(final HttpStatus status, final String message) {
        this.status = status;
        this.message = message;
        data = null;
    }

    public static <T> ApiResponse<T> of(final HttpStatus status, final String message) {
        return new ApiResponse<>(status, message);
    }

    public static <T> ApiResponse<T> of(final HttpStatus status, final String message, final T data) {
        return new ApiResponse<>(status, message, data);
    }

    public static <T> ApiResponse<T> success(final SuccessCode successCode) {
        return new ApiResponse<>(successCode.getStatus(), successCode.getMessage());
    }

    public static <T> ApiResponse<T> success(final SuccessCode successCode, final T data) {
        return new ApiResponse<>(successCode.getStatus(), successCode.getMessage(), data);
    }

    public static <T> ApiResponse<T> error(final ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getStatus(), errorCode.getMessage());
    }

    public static <T> ApiResponse<T> error(final ErrorCode errorCode, final String message) {
        return new ApiResponse<>(errorCode.getStatus(), message);
    }

    public static <T> ApiResponse<T> error(final ErrorCode errorCode, final String message, final T data) {
        return new ApiResponse<>(errorCode.getStatus(), message, data);
    }

}
