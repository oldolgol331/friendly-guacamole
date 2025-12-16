package com.example.demo.domain.account.dao;

import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.model.AccountStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.example.demo.domain.account.dao
 * FileName    : AccountRepository
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : Account 엔티티 DAO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
public interface AccountRepository extends JpaRepository<Account, UUID>, AccountRepositoryCustom {

    Optional<Account> findByIdAndStatus(UUID id, AccountStatus status);

    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndStatusAndDeletedAtNull(String email, AccountStatus status);

    boolean existsByIdAndStatus(UUID id, AccountStatus status);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    void deleteByEmail(String email);

}
