package com.example.demo.domain.account.dao;

import com.example.demo.domain.account.model.OAuthConnection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PackageName : com.example.demo.domain.account.dao
 * FileName    : OAuthConnectionRepository
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : OAuthConnection 엔티티 DAO
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
public interface OAuthConnectionRepository extends JpaRepository<OAuthConnection, Long> {

    Optional<OAuthConnection> findByProviderAndProviderId(String provider, String providerId);

}
