package com.example.demo.common.error;

import static com.example.demo.common.response.ErrorCode.ACCESS_DENIED;
import static com.example.demo.common.response.ErrorCode.ENTITY_NOT_FOUND;
import static com.example.demo.common.response.ErrorCode.INTERNAL_SERVER_ERROR;
import static com.example.demo.common.response.ErrorCode.INVALID_INPUT_VALUE;
import static com.example.demo.common.response.ErrorCode.METHOD_ARGUMENT_TYPE_MISMATCH;
import static com.example.demo.common.response.ErrorCode.METHOD_NOT_ALLOWED;
import static com.example.demo.common.response.ErrorCode.METHOD_NOT_SUPPORTED;
import static com.example.demo.common.response.ErrorCode.RESOURCE_NOT_FOUND;
import static com.example.demo.common.response.ErrorCode.UNAUTHORIZED;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * PackageName : com.example.demo.common.error
 * FileName    : GlobalExceptionHandler
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 공통 예외 처리 핸들러
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException e
    ) {
        log.error("handleMethodArgumentNotValidException: {}", e.getMessage(), e);
        final ErrorCode errorCode = INVALID_INPUT_VALUE;
        final String errorMessage = e.getBindingResult()
                                     .getAllErrors()
                                     .stream()
                                     .map(ObjectError::getDefaultMessage)
                                     .collect(Collectors.joining(", "));
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(final BindException e) {
        log.error("handleBindException: {}", e.getMessage(), e);
        final ErrorCode errorCode = INVALID_INPUT_VALUE;
        final String errorMessage = e.getBindingResult()
                                     .getAllErrors()
                                     .stream()
                                     .map(ObjectError::getDefaultMessage)
                                     .collect(Collectors.joining(", "));
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            final MethodArgumentTypeMismatchException e
    ) {
        log.error("handleMethodArgumentTypeMismatchException: {} for property {}", e.getMessage(), e.getName(), e);
        final ErrorCode errorCode = METHOD_ARGUMENT_TYPE_MISMATCH;
        final String errorMessage = String.format("요청 인자 '%s'의 타입이 올바르지 않습니다. 예상 타입: %s",
                                                  e.getName(),
                                                  e.getRequiredType() != null ? e.getRequiredType().getName()
                                                                              : "알 수 없음");
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ApiResponse<Void>> handleServletRequestBindingException(
            final ServletRequestBindingException e
    ) {
        log.error("handleServletRequestBindingException: {}", e.getMessage(), e);
        final ErrorCode errorCode    = INVALID_INPUT_VALUE;
        final String    errorMessage = "필수 요청 파라미터 또는 헤더가 누락되었습니다.";
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException(
            final HttpRequestMethodNotSupportedException e
    ) {
        log.error("handleHttpRequestMethodNotSupportedException: {}", e.getMessage(), e);
        final ErrorCode errorCode = METHOD_NOT_ALLOWED;
        final String supportedMethods = e.getSupportedHttpMethods() != null
                                        ? e.getSupportedHttpMethods()
                                           .stream()
                                           .map(HttpMethod::name)
                                           .collect(Collectors.joining(", "))
                                        : "알 수 없음";
        final String errorMessage = String.format("요청하신 HTTP Method '%s'는 이 리소스에서 지원되지 않습니다. 지원되는 Method: [%s]",
                                                  e.getMethod(),
                                                  supportedMethods);
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException(
            final HttpMediaTypeNotSupportedException e
    ) {
        log.error("handleHttpMediaTypeNotSupportedException: {}", e.getMessage(), e);
        final ErrorCode errorCode = METHOD_NOT_SUPPORTED;
        final String supportedTypes = e.getSupportedMediaTypes() != null
                                      ? e.getSupportedMediaTypes()
                                         .stream()
                                         .map(Object::toString)
                                         .collect(Collectors.joining(", "))
                                      : "알 수 없음";
        final String errorMessage = String.format("요청하신 Content-Type '%s'는 지원되지 않습니다. 지원되는 Content-Type: [%s]",
                                                  e.getContentType(),
                                                  supportedTypes);
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode, errorMessage));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(final NoResourceFoundException e) {
        log.error("handleNoResourceFoundException: {}", e.getMessage(), e);
        final ErrorCode errorCode = RESOURCE_NOT_FOUND;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleEntityNotFoundException(final EntityNotFoundException e) {
        log.error("handleEntityNotFoundException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ENTITY_NOT_FOUND;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(
            final DataIntegrityViolationException e
    ) {
        final ErrorCode errorCode = INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(final AccessDeniedException e) {
        log.error("handleAccessDeniedException: {}", e.getMessage(), e);
        final ErrorCode errorCode = ACCESS_DENIED;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(final AuthenticationException e) {
        log.error("handleAuthenticationException: {}", e.getMessage(), e);
        final ErrorCode errorCode = UNAUTHORIZED;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(final BusinessException e) {
        log.error("handleCustomException: {}", e.getMessage(), e);
        return ResponseEntity.status(e.getErrorCode().getStatus())
                             .body(ApiResponse.error(e.getErrorCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(final Exception e) {
        log.error("handleException: {}", e.getMessage(), e);
        final ErrorCode errorCode = INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(errorCode.getStatus())
                             .body(ApiResponse.error(errorCode));
    }

}
