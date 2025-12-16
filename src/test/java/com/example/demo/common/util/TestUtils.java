package com.example.demo.common.util;

import static com.example.demo.domain.account.constant.AccountConst.PASSWORD_PATTERN;
import static lombok.AccessLevel.PRIVATE;

import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.model.OAuthConnection;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;

/**
 * PackageName : com.example.demo.common.util
 * FileName    : TestUtils
 * Author      : oldolgol331
 * Date        : 25. 12. 16.
 * Description : 테스트용 유틸
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 16.   oldolgol331          Initial creation
 */
@NoArgsConstructor(access = PRIVATE)
public abstract class TestUtils {

    public static final FixtureMonkey FIXTURE_MONKEY = FixtureMonkey.builder()
                                                                    .objectIntrospector(
                                                                            new FailoverIntrospector(
                                                                                    List.of(ConstructorPropertiesArbitraryIntrospector.INSTANCE,
                                                                                            BuilderArbitraryIntrospector.INSTANCE,
                                                                                            FieldReflectionArbitraryIntrospector.INSTANCE,
                                                                                            BeanArbitraryIntrospector.INSTANCE),
                                                                                    false
                                                                            )
                                                                    )
                                                                    .plugin(new JakartaValidationPlugin())
                                                                    .defaultNotNull(true)
                                                                    .nullableContainer(false)
                                                                    .nullableElement(false)
                                                                    .build();

    public static final Faker FAKER = new Faker(new Locale.Builder().setLanguage("en").build(), new Random());

    public static List<Account> createAccounts(final int size) {
        return FIXTURE_MONKEY.giveMeBuilder(Account.class)
                             .instantiate(Instantiator.factoryMethod("of")
                                                      .parameter(String.class, "email")
                                                      .parameter(String.class, "password")
                                                      .parameter(String.class, "nickname"))
                             .setLazy("email", () -> FAKER.internet().safeEmailAddress())
                             .setLazy("password", TestUtils::createPassword)
                             .setLazy("nickname", () -> FAKER.credentials().username().replace(".", "").substring(0, 5))
                             .sampleList(size);
    }

    public static Account createAccount() {
        return createAccounts(1).getFirst();
    }

    public static List<OAuthConnection> createOAuthConnections(final Account account, final int size) {
        return IntStream.range(0, size)
                        .mapToObj(
                                i -> OAuthConnection.of(account, FAKER.company().name(), UUID.randomUUID().toString())
                        )
                        .toList();
    }

    public static OAuthConnection createOAuthConnection(final Account account) {
        return createOAuthConnections(account, 1).getFirst();
    }

    public static String createPassword() {
        while (true) {
            String password = FAKER.credentials().password(8, 13, true, true, true);
            if (PASSWORD_PATTERN.matcher(password).matches()) return password;
        }
    }

}
