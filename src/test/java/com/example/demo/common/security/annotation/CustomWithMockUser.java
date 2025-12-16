package com.example.demo.common.security.annotation;

import static com.example.demo.domain.account.model.AccountRole.USER;
import static com.example.demo.domain.account.model.AccountStatus.ACTIVE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import com.example.demo.common.security.CustomWithSecurityContextFactory;
import com.example.demo.domain.account.model.AccountRole;
import com.example.demo.domain.account.model.AccountStatus;
import java.lang.annotation.Retention;
import org.springframework.security.test.context.support.WithSecurityContext;

/**
 * PackageName : com.example.demo.common.security.annotation
 * FileName    : CustomWithMockUser
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 테스트용 인증(Authentication) 어노테이션
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@Retention(RUNTIME)
@WithSecurityContext(factory = CustomWithSecurityContextFactory.class)
public @interface CustomWithMockUser {

    String id() default "0199985a-f612-d277-a30b-2984051e198d";

    String email() default "test@example.com";

    String password() default "Test12!@";

    AccountRole role() default USER;

    AccountStatus status() default ACTIVE;

}
