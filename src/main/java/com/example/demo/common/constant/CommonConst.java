package com.example.demo.common.constant;

import static lombok.AccessLevel.PRIVATE;

import java.util.regex.Pattern;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.example.demo.common.constant
 * FileName    : CommonConst
 * Author      : oldolgol331
 * Date        : 25. 12. 21.
 * Description : 공통 상수
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 21.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class CommonConst {

    public static final String IP_ADDRESS_REGEX = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    public static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(IP_ADDRESS_REGEX);

}
