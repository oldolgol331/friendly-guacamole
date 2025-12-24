package com.example.demo.infra.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * PackageName : com.example.demo.infra.config
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
        // 커넥션 설정
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                                                            .setConnectTimeout(Timeout.ofMilliseconds(timeoutMillis))
                                                            .build();

        // 커넥션 풀 설정
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(100); // 전체 최대 연결 수
        connectionManager.setDefaultMaxPerRoute(20);    // 각 호스트(IP)당 최대 연결 수
        connectionManager.setDefaultConnectionConfig(connectionConfig);

        // 요청 타임아웃 설정
        RequestConfig requestConfig = RequestConfig.custom()
                                                   .setConnectionRequestTimeout(
                                                           Timeout.ofMilliseconds(timeoutMillis)
                                                   )  // 커넥션 풀에서 연결을 빌려오는 데 기다리는 시간
                                                   .setResponseTimeout(
                                                           Timeout.ofMilliseconds(timeoutMillis)
                                                   )   // 서버로부터 응답(패킷)이 오는 데 걸리는 시간
                                                   .build();

        // Apache HttpClient 생성
        CloseableHttpClient httpClient = HttpClients.custom()
                                                    .setConnectionManager(connectionManager)
                                                    .setDefaultRequestConfig(requestConfig)
                                                    .build();

        // 스프링용 팩토리로 감싸기
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // RestClient 빌드 및 반환
        return builder.requestFactory(factory)
                      .baseUrl(url)
                      .defaultHeader("Authorization", "PortOne " + secretKey)
                      .defaultHeader("Content-Type", "application/json")
                      .build();
    }

}
