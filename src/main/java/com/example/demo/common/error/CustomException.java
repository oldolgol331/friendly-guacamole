package com.example.demo.common.error;

import com.example.demo.common.response.ErrorCode;
import lombok.Getter;

/**
 * PackageName : com.example.demo.common.error
 * FileName    : CustomException
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 커스텀 예외
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Getter
public class CustomException extends RuntimeException {

    private final ErrorCode errorCode;

    public CustomException(final ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CustomException(final ErrorCode errorCode, final String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
