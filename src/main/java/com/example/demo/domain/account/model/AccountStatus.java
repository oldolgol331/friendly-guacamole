package com.example.demo.domain.account.model;

/**
 * PackageName : com.example.demo.domain.account.model
 * FileName    : AccountStatus
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 계정 상태
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
public enum AccountStatus {
    INACTIVE,   // 비활성
    ACTIVE,     // 활성
    DELETED,    // 탈퇴함
    BLOCKED     // 차단됨
}
