package com.github.bademux.ghfetcher.client;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RootUriTemplateHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM;

@Configuration
public class GithubClientConfig {


    @Configuration
    public static class Cache {

        @Bean
        static CloseableHttpClient ghHttpClient(@NonNull @Value("${githubClient.cache.maxObjectSize}") Integer maxObjectSize,
                                                @NonNull @Value("${githubClient.cache.maxEntries}") Integer maxCacheEntries,
                                                @NonNull @Value("${githubClient.connection.maxTotal}") Integer maxTotalConnections,
                                                @NonNull @Value("${githubClient.connection.timeout}") Integer timeout,
                                                Clock clock) {
            CacheConfig cacheConfig = createCacheConfig(maxObjectSize, maxCacheEntries);
            var config = RequestConfig.custom()
                    .setConnectTimeout(timeout * 1000)
                    .setConnectionRequestTimeout(timeout * 1000)
                    .setSocketTimeout(timeout * 1000).build();
            return RateLimitCacheHttpClientBuilder
                    .create()
                    .setClock(clock)
                    .setCacheConfig(cacheConfig)
                    .setConnectionManagerShared(true)
                    .setMaxConnTotal(maxTotalConnections)
                    .setMaxConnPerRoute(1)
                    .setDefaultRequestConfig(config)
                    //.setHttpCacheStorage(SpringBasedCache)  // can be used distributed impl or fused with org.springframework.cache.Cache
                    .build();
        }

        private static CacheConfig createCacheConfig(Integer maxObjectSize, Integer maxCacheEntries) {
            return CacheConfig
                    .custom()
                    .setMaxObjectSize(maxObjectSize * 1024)
                    .setMaxCacheEntries(maxCacheEntries)
                    .build();
        }
    }


    @Configuration
    public static class HttpClient {

        @Bean
        static ClientHttpRequestFactory ghRequestFactory(CloseableHttpClient ghHttpClient) {
            return new HttpComponentsClientHttpRequestFactory(ghHttpClient);
        }

        @ConditionalOnProperty(value = "githubClient.auth.type", havingValue = "basic")
        @Bean
        public static ClientHttpRequestInterceptor basicAuthRequestInterceptor(@NonNull @Value("${githubClient.auth.basic.name}") String name,
                                                                               @NonNull @Value("${githubClient.auth.basic.password}") String password) {
            return new BasicAuthenticationInterceptor(name, password);
        }

        @Bean
        public static RestTemplate ghRestTemplate(ClientHttpRequestFactory ghRequestFactory,
                                                  List<ClientHttpRequestInterceptor> clientHttpRequestInterceptors,
                                                  @NonNull @Value("${githubClient.rootUri}") URI rootUri) {
            RestTemplate restTemplate = new RestTemplate(ghRequestFactory);
            fixNotChangedResponseForApacheHttpClient(restTemplate);
            RootUriTemplateHandler.addTo(restTemplate, rootUri.toString());
            restTemplate.setInterceptors(clientHttpRequestInterceptors);
            return restTemplate;
        }

        @Bean
        GithubClient gitHubClient(RestTemplate githubRestTemplate) {
            return new GithubClientV3(githubRestTemplate);
        }

        /**
         * Assume we have a HttpMessageConverter that extends AbstractHttpMessageConverter and supports application/json
         * <p>
         * and supports APPLICATION_JSON contentType
         * Unfortunately Apache HTTP client overrides original githubs's application/json content type on validating ETag call,
         * So we need to support application/octet-stream returned by validating call alog with 304 status.
         *
         * @param restTemplate
         */
        @SuppressWarnings("unchecked")
        static void fixNotChangedResponseForApacheHttpClient(RestTemplate restTemplate) {
            restTemplate.getMessageConverters().stream()
                    .filter(converter -> converter.getSupportedMediaTypes().contains(APPLICATION_JSON))
                    .findFirst()
                    .map(httpMessageConverter -> (AbstractHttpMessageConverter<Object>) httpMessageConverter)
                    .ifPresent(converter -> {
                        List<MediaType> mediaTypes = new ArrayList<>(converter.getSupportedMediaTypes());
                        mediaTypes.add(APPLICATION_OCTET_STREAM);
                        converter.setSupportedMediaTypes(mediaTypes);
                    });
        }

    }

}