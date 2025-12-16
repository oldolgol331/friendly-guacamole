package com.example.demo.common.security.service;

import static com.example.demo.domain.account.model.AccountStatus.ACTIVE;

import com.example.demo.common.security.model.CustomUserDetails;
import com.example.demo.domain.account.dao.AccountRepository;
import com.example.demo.domain.account.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * PackageName : com.example.demo.common.security.service
 * FileName    : CustomUserDetailsService
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 커스텀 UserDetails 서비스
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmailAndStatusAndDeletedAtNull(username.toLowerCase(), ACTIVE)
                                           .orElseThrow(() -> new UsernameNotFoundException(
                                                   "계정이 존재하지 않습니다: " + username.toLowerCase()
                                           ));
        return CustomUserDetails.of(account.getId(),
                                    account.getEmail(),
                                    account.getPassword(),
                                    account.getRole(),
                                    account.getStatus());
    }

}
