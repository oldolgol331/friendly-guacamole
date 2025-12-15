package com.example.demo.domain.account.dao;

import com.example.demo.domain.account.dto.AccountResponse.AccountInfoResponse;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.model.AccountStatus;
import java.util.Optional;
import java.util.UUID;

/**
 * PackageName : com.example.demo.domain.account.dao
 * FileName    : AccountRepositoryCustom
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : Account 엔티티 커스텀 DAO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
public interface AccountRepositoryCustom {

    Optional<Account> findByProviderAndProviderId(String provider, String providerId);

    Optional<AccountInfoResponse> getAccountInfoResponseById(UUID id);

    Optional<AccountInfoResponse> getAccountInfoResponseByIdAndStatus(UUID id, AccountStatus status);

}
