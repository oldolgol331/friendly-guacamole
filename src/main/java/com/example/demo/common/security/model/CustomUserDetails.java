package com.example.demo.common.security.model;

import static com.example.demo.domain.account.model.AccountStatus.ACTIVE;
import static com.example.demo.domain.account.model.AccountStatus.BLOCKED;
import static lombok.AccessLevel.PRIVATE;

import com.example.demo.domain.account.model.AccountRole;
import com.example.demo.domain.account.model.AccountStatus;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * PackageName : com.example.demo.common.security.model
 * FileName    : CustomUserDetails
 * Author      : oldolgol331
 * Date        : 25. 12. 15.
 * Description : 커스텀 UserDetails
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 15.   oldolgol331          Initial creation
 */
@RequiredArgsConstructor(access = PRIVATE)
public class CustomUserDetails implements UserDetails, OAuth2User {

    @Getter
    private final UUID                id;
    private final String              email;
    private final String              password;
    @Getter
    private final AccountRole         role;
    private final AccountStatus       status;
    private final Map<String, Object> attributes;

    private CustomUserDetails(final UUID id,
                              final String email,
                              final String password,
                              final AccountRole role,
                              final AccountStatus status) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        attributes = null;
    }

    private CustomUserDetails(final UUID id,
                              final String email,
                              final AccountRole role,
                              final AccountStatus status,
                              final Map<String, Object> attributes) {
        this.id = id;
        this.email = email;
        password = null;
        this.role = role;
        this.status = status;
        this.attributes = attributes;
    }

    public static CustomUserDetails of(final UUID id,
                                       final String email,
                                       final String password,
                                       final AccountRole role,
                                       final AccountStatus status) {
        return new CustomUserDetails(id, email, password, role, status);
    }

    public static CustomUserDetails of(final UUID id,
                                       final String email,
                                       final AccountRole role,
                                       final AccountStatus status,
                                       final Map<String, Object> attributes) {
        return new CustomUserDetails(id, email, role, status, attributes);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.getRoleValue()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !(status != ACTIVE && status == BLOCKED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == ACTIVE;
    }

    @Override
    public String getName() {
        return String.valueOf(attributes.get("id"));
    }

}
