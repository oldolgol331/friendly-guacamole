package com.example.demo.infra.payment.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * PackageName : com.example.demo.infra.payment.config
 * FileName    : RestClientConfig
 * Author      : oldolgol331
 * Date        : 25. 12. 19.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 25. 12. 19.   oldolgol331          Initial creation
 */
@Configuration
public class RestClientConfig {

    @Bean("portOneRestClient")
    public RestClient portOneRestClient(final RestClient.Builder builder,
                                        @Value("${payment.portone.api.url}") final String url,
                                        @Value("${payment.portone.api.secret-key}") final String secretKey,
                                        @Value("${payment.portone.api.timeout-millis}") final int timeoutMillis) {
        return builder.baseUrl(url)
                      .defaultHeader("Authorization", "PortOne " + secretKey)
                      .defaultHeader("Content-Type", "application/json")
                      .requestFactory(new HttpComponentsClientHttpRequestFactory() {
                          @Override
                          public void setConnectionRequestTimeout(Duration connectionRequestTimeout) {
                              super.setConnectionRequestTimeout(Duration.ofMillis(timeoutMillis));
                          }
                      })
                      .build();
    }

}
