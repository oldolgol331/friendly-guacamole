package com.example.demo.domain.account.model;

import static lombok.AccessLevel.PRIVATE;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * PackageName : com.example.demo.domain.account.model
 * FileName    : AccountRole
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 계정 권한
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Getter
@RequiredArgsConstructor(access = PRIVATE)
public enum AccountRole {
    USER("ROLE_USER"),      // 일반 계정
    ADMIN("ROLE_ADMIN");    // 관리자 계정

    private final String roleValue;
}
