package com.example.demo.common.response.advice;

import com.example.demo.common.response.ApiResponse;
import com.example.demo.common.response.annotation.CustomPageResponse;
import com.example.demo.common.response.util.PageResponseConverter;
import java.util.Map;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * PackageName : com.example.demo.common.response.advice
 * FileName    : PageResponseAdvice
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 커스텀 페이징 어드바이스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@RestControllerAdvice
public class PageResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return returnType.hasMethodAnnotation(CustomPageResponse.class);
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {
        CustomPageResponse annotation = returnType.getMethodAnnotation(CustomPageResponse.class);
        if (annotation == null) return body;

        Page<?>        page        = null;
        ApiResponse<?> apiResponse = null;

        if (body instanceof Page) page = (Page<?>) body;
        else if (body instanceof ApiResponse) {
            apiResponse = (ApiResponse<?>) body;
            if (apiResponse.getData() instanceof Page) page = (Page<?>) apiResponse.getData();
        }

        if (page == null) return body;

        Map<String, Object> customPageResponse = PageResponseConverter.convertPageToCustomMap(page, annotation);

        if (body instanceof Page) return customPageResponse;
        else return ApiResponse.of(apiResponse.getStatus(), apiResponse.getMessage(), customPageResponse);
    }

}
