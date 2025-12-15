package com.example.demo.common.response.util;

import static lombok.AccessLevel.PRIVATE;

import com.example.demo.common.response.annotation.CustomPageResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * PackageName : com.example.demo.common.response.util
 * FileName    : PageResponseConverter
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 커스텀 페이징 응답 변환
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class PageResponseConverter {

    public static Map<String, Object> convertPageToCustomMap(final Page<?> page, final CustomPageResponse annotation) {
        Map<String, Object> map = new HashMap<>();
        if (annotation.content()) map.put("content", page.getContent());
        if (annotation.totalElements()) map.put("total_elements", page.getTotalElements());
        if (annotation.totalPages()) map.put("total_pages", page.getTotalPages());
        if (annotation.size()) map.put("size", page.getSize());
        if (annotation.number()) map.put("number", page.getNumber());
        if (annotation.numberOfElements()) map.put("number_of_elements", page.getNumberOfElements());
        if (annotation.sort()) map.put("sort", page.getSort());
        if (annotation.empty()) map.put("empty", page.isEmpty());
        if (annotation.hasContent()) map.put("has_content", page.hasContent());
        if (annotation.first()) map.put("first", page.isFirst());
        if (annotation.last()) map.put("last", page.isLast());
        if (annotation.hasPrevious()) map.put("has_previous", page.hasPrevious());
        if (annotation.hasNext()) map.put("has_next", page.hasNext());
        return map;
    }

}
