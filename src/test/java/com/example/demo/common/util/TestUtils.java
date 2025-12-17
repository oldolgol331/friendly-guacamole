package com.example.demo.common.util;

import static com.example.demo.domain.account.constant.AccountConst.PASSWORD_PATTERN;
import static lombok.AccessLevel.PRIVATE;

import com.example.demo.domain.account.dto.AccountRequest.AccountPasswordUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountSignInRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountSignUpRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountUpdateRequest;
import com.example.demo.domain.account.dto.AccountRequest.AccountWithdrawRequest;
import com.example.demo.domain.account.dto.AccountRequest.PasswordResetConfirmRequest;
import com.example.demo.domain.account.dto.AccountResponse.AccountInfoResponse;
import com.example.demo.domain.account.model.Account;
import com.example.demo.domain.account.model.AccountRole;
import com.example.demo.domain.account.model.AccountStatus;
import com.example.demo.domain.account.model.OAuthConnection;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceCreateRequest;
import com.example.demo.domain.performance.dto.PerformanceRequest.PerformanceUpdateRequest;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceDetailResponse;
import com.example.demo.domain.performance.dto.PerformanceResponse.PerformanceListResponse;
import com.example.demo.domain.performance.model.Performance;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.BuilderArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FailoverIntrospector;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.NoArgsConstructor;
import net.datafaker.Faker;
import net.jqwik.api.Arbitraries;

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

    public static AccountSignInRequest createAccountSignInRequest() {
        return FIXTURE_MONKEY.giveMeBuilder(AccountSignInRequest.class)
                             .instantiate(Instantiator.constructor()
                                                      .parameter(String.class, "email")
                                                      .parameter(String.class, "password"))
                             .setLazy("email", () -> FAKER.internet().safeEmailAddress())
                             .setLazy("password", TestUtils::createPassword)
                             .sample();
    }

    public static AccountSignUpRequest createAccountSignUpRequest() {
        String password = createPassword();
        return FIXTURE_MONKEY.giveMeBuilder(AccountSignUpRequest.class)
                             .instantiate(Instantiator.constructor()
                                                      .parameter(String.class, "email")
                                                      .parameter(String.class, "password")
                                                      .parameter(String.class, "confirmPassword")
                                                      .parameter(String.class, "nickname"))
                             .setLazy("email", () -> FAKER.internet().safeEmailAddress())
                             .set("password", password)
                             .set("confirmPassword", password)
                             .setLazy("nickname", () -> FAKER.credentials().username().replace(".", "").substring(0, 5))
                             .sample();
    }

    public static AccountInfoResponse createAccountInfoResponse() {
        return FIXTURE_MONKEY.giveMeBuilder(AccountInfoResponse.class)
                             .instantiate(Instantiator.constructor()
                                                      .parameter(UUID.class, "id")
                                                      .parameter(String.class, "email")
                                                      .parameter(String.class, "nickname")
                                                      .parameter(AccountRole.class, "role")
                                                      .parameter(AccountStatus.class, "status")
                                                      .parameter(List.class, "providers")
                                                      .parameter(LocalDateTime.class, "createdAt")
                                                      .parameter(LocalDateTime.class, "updatedAt"))
                             .setLazy("id", UUID::randomUUID)
                             .setLazy("email", () -> FAKER.internet().emailAddress())
                             .setLazy("nickname", () -> FAKER.credentials().username().replace(".", "").substring(0, 5))
                             .set("role", Arbitraries.of(AccountRole.USER, AccountRole.ADMIN))
                             .set("status", AccountStatus.ACTIVE)
                             .set("providers", Collections.emptyList())
                             .setLazy("createdAt",
                                      () -> LocalDateTime.ofInstant(FAKER.timeAndDate().past(365, TimeUnit.DAYS),
                                                                    ZoneId.systemDefault()))
                             .setLazy("updatedAt",
                                      () -> LocalDateTime.ofInstant(FAKER.timeAndDate().past(365, TimeUnit.DAYS),
                                                                    ZoneId.systemDefault()))
                             .sample();
    }

    public static AccountUpdateRequest createAccountUpdateRequest(final String currentPassword) {
        return FIXTURE_MONKEY.giveMeBuilder(AccountUpdateRequest.class)
                             .instantiate(Instantiator.constructor()
                                                      .parameter(String.class, "newNickname")
                                                      .parameter(String.class, "currentPassword"))
                             .setLazy("newNickname",
                                      () -> "updated" + FAKER.credentials().username().replace(".", "").substring(0, 5))
                             .set("currentPassword", currentPassword)
                             .sample();
    }

    public static AccountPasswordUpdateRequest createAccountPasswordUpdateRequest(final String currentPassword) {
        String newPassword = createPassword();
        return FIXTURE_MONKEY.giveMeBuilder(AccountPasswordUpdateRequest.class)
                             .instantiate(Instantiator.constructor()
                                                      .parameter(String.class, "newPassword")
                                                      .parameter(String.class, "confirmNewPassword")
                                                      .parameter(String.class, "currentPassword"))
                             .set("newPassword", newPassword)
                             .set("confirmNewPassword", newPassword)
                             .set("currentPassword", currentPassword)
                             .sample();
    }

    public static PasswordResetConfirmRequest createPasswordResetConfirmRequest() {
        String newPassword = createPassword();
        return FIXTURE_MONKEY.giveMeBuilder(PasswordResetConfirmRequest.class)
                             .instantiate(Instantiator.constructor()
                                                      .parameter(String.class, "newPassword")
                                                      .parameter(String.class, "confirmNewPassword"))
                             .set("newPassword", newPassword)
                             .set("confirmNewPassword", newPassword)
                             .sample();
    }

    public static AccountWithdrawRequest createAccountWithdrawRequest(final String currentPassword) {
        return new AccountWithdrawRequest(currentPassword);
    }

    public static String createPassword() {
        while (true) {
            String password = FAKER.credentials().password(8, 13, true, true, true);
            if (PASSWORD_PATTERN.matcher(password).matches()) return password;
        }
    }

    public static List<Performance> createPerformances(final int size) {
        LocalDateTime startTime = FAKER.timeAndDate()
                                       .future()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalDateTime();
        return FIXTURE_MONKEY.giveMeBuilder(Performance.class)
                             .instantiate(Instantiator.factoryMethod("of")
                                                      .parameter(String.class, "name")
                                                      .parameter(String.class, "venue")
                                                      .parameter(String.class, "info")
                                                      .parameter(LocalDateTime.class, "startTime")
                                                      .parameter(LocalDateTime.class, "endTime"))
                             .setLazy("name", () -> FAKER.hobby().activity())
                             .setLazy("venue", () -> FAKER.address().fullAddress())
                             .setLazy("info", () -> FAKER.lorem().characters(1, 65535, true, true, true))
                             .setLazy("startTime", () -> startTime)
                             .setLazy("endTime", () -> startTime.plusHours(3))
                             .sampleList(size);
    }

    public static Performance createPerformance() {
        return createPerformances(1).getFirst();
    }

    public static PerformanceCreateRequest createPerformanceCreateRequest() {
        LocalDateTime startTime = FAKER.timeAndDate()
                                       .future()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalDateTime();
        return FIXTURE_MONKEY.giveMeBuilder(PerformanceCreateRequest.class)
                             .instantiate(Instantiator.constructor()
                                                      .parameter(String.class, "name")
                                                      .parameter(String.class, "venue")
                                                      .parameter(String.class, "info")
                                                      .parameter(LocalDateTime.class, "startTime")
                                                      .parameter(LocalDateTime.class, "endTime")
                                                      .parameter(int.class, "totalSeats")
                                                      .parameter(int.class, "price"))
                             .setLazy("name", () -> FAKER.hobby().activity())
                             .setLazy("venue", () -> FAKER.address().fullAddress())
                             .setLazy("info", () -> FAKER.lorem().characters(1, 65535, true, true, true))
                             .setLazy("startTime", () -> startTime)
                             .setLazy("endTime", () -> startTime.plusHours(3))
                             .setLazy("totalSeats", () -> FAKER.number().numberBetween(1, 500))
                             .setLazy("price", () -> FAKER.number().numberBetween(0, Integer.MAX_VALUE))
                             .sample();
    }

    public static PerformanceUpdateRequest createPerformanceUpdateRequest() {
        LocalDateTime startTime = FAKER.timeAndDate()
                                       .future()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalDateTime();
        return FIXTURE_MONKEY.giveMeBuilder(PerformanceUpdateRequest.class)
                             .instantiate(Instantiator.constructor()
                                                      .parameter(String.class, "name")
                                                      .parameter(String.class, "venue")
                                                      .parameter(String.class, "info")
                                                      .parameter(LocalDateTime.class, "startTime")
                                                      .parameter(LocalDateTime.class, "endTime"))
                             .setLazy("name", () -> FAKER.hobby().activity())
                             .setLazy("venue", () -> FAKER.address().fullAddress())
                             .setLazy("info", () -> FAKER.lorem().characters(1, 65535, true, true, true))
                             .setLazy("startTime", () -> startTime)
                             .setLazy("endTime", () -> startTime.plusHours(3))
                             .sample();
    }

    public static List<PerformanceListResponse> createPerformanceListResponses(final int size) {
        LocalDateTime startTime = FAKER.timeAndDate()
                                       .future()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalDateTime();
        return FIXTURE_MONKEY.giveMeBuilder(PerformanceListResponse.class)
                             .instantiate(Instantiator.constructor()
                                                      .parameter(Long.class, "id")
                                                      .parameter(String.class, "name")
                                                      .parameter(String.class, "venue")
                                                      .parameter(LocalDateTime.class, "startTime")
                                                      .parameter(LocalDateTime.class, "endTime")
                                                      .parameter(int.class, "remainingSeats")
                                                      .parameter(int.class, "totalSeats")
                                                      .parameter(LocalDateTime.class, "createdAt")
                                                      .parameter(LocalDateTime.class, "updatedAt"))
                             .setLazy("name", () -> FAKER.hobby().activity())
                             .setLazy("venue", () -> FAKER.address().fullAddress())
                             .setLazy("startTime", () -> startTime)
                             .setLazy("endTime", () -> startTime.plusHours(3))
                             .setLazy("totalSeats", () -> FAKER.number().numberBetween(1, Integer.MAX_VALUE))
                             .setLazy("price", () -> FAKER.number().numberBetween(0, Integer.MAX_VALUE))
                             .setLazy("createdAt", () -> startTime.minusWeeks(2))
                             .setLazy("updatedAt", () -> startTime.minusWeeks(2))
                             .sampleList(size);
    }

    public static PerformanceDetailResponse createPerformanceDetailResponse() {
        LocalDateTime startTime = FAKER.timeAndDate()
                                       .future()
                                       .atZone(ZoneId.systemDefault())
                                       .toLocalDateTime();
        return FIXTURE_MONKEY.giveMeBuilder(PerformanceDetailResponse.class)
                             .instantiate(Instantiator.constructor()
                                                      .parameter(Long.class, "id")
                                                      .parameter(String.class, "name")
                                                      .parameter(String.class, "venue")
                                                      .parameter(String.class, "info")
                                                      .parameter(LocalDateTime.class, "startTime")
                                                      .parameter(LocalDateTime.class, "endTime")
                                                      .parameter(int.class, "remainingSeats")
                                                      .parameter(int.class, "totalSeats")
                                                      .parameter(LocalDateTime.class, "createdAt")
                                                      .parameter(LocalDateTime.class, "updatedAt"))
                             .setLazy("name", () -> FAKER.hobby().activity())
                             .setLazy("venue", () -> FAKER.address().fullAddress())
                             .setLazy("info", () -> FAKER.lorem().characters(1, 65535, true, true, true))
                             .setLazy("startTime", () -> startTime)
                             .setLazy("endTime", () -> startTime.plusHours(3))
                             .setLazy("totalSeats", () -> FAKER.number().numberBetween(1, Integer.MAX_VALUE))
                             .setLazy("price", () -> FAKER.number().numberBetween(0, Integer.MAX_VALUE))
                             .setLazy("createdAt", () -> startTime.minusWeeks(2))
                             .setLazy("updatedAt", () -> startTime.minusWeeks(2))
                             .sample();
    }

}
