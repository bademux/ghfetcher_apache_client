package com.github.bademux.ghfetcher

import com.github.bademux.ghfetcher.client.GithubClient
import com.github.bademux.ghfetcher.client.GithubClientConfig
import com.github.bademux.ghfetcher.client.RateLimitCacheHttpClientBuilder
import com.github.bademux.ghfetcher.client.RateLimitException
import com.github.bademux.ghfetcher.utils.RecordingWireMock
import com.github.bademux.ghfetcher.utils.WiremockConfig
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.junit.WireMockClassRule
import org.apache.http.impl.client.CloseableHttpClient
import org.junit.ClassRule
import org.junit.rules.RuleChain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import spock.lang.Narrative
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise
import spock.mock.DetachedMockFactory

import java.lang.Object as Should
import java.time.Clock

import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import static java.time.Instant.EPOCH
import static java.util.UUID.fromString

@Narrative("""
Test kind of circuitBreaker/rate limiter  "RateLimitingExec"
""")
@ContextConfiguration(classes = [GithubClientConfig, TestConfig])
@TestPropertySource(properties = [
        'githubClient.cache.maxObjectSize=12',
        'githubClient.cache.maxEntries=1200',
        'githubClient.connection.maxTotal=40'
])
@Stepwise
class WebClientHandleRateLimitSpec extends Specification {

    final static WireMockClassRule wireMock = new WireMockClassRule(wireMockConfig()
            .withRootDirectory("src/integrationTest/resources/${WebClientHandleRateLimitSpec.simpleName}")
            .dynamicPort()
    )

    @Shared
    @ClassRule
    RuleChain chain = RuleChain.outerRule(wireMock)
            .around(new RecordingWireMock(wireMock, 'https://api.github.com/'))

    @Autowired
    GithubClient client
    @Autowired
    Clock clock

    Should 'fail with Rate limit'() {
        given:
        def rateLimitExceeded = 'e86d368c-6e58-486b-8671-7afa5c4fc28b'
        clock.millis() >> getRateLimitFromStubUUID(rateLimitExceeded)
        when: 'do 1st request'
        client.findByRepoOwnerAndName('octokit', 'octokit.rb')
        then:
        thrown(HttpClientErrorException.Forbidden)
        and:
        wireMock.verify(1, getRequestedFor(urlEqualTo('/repos/octokit/octokit.rb')))
        wireMock.allServeEvents.size() == 1
    }

    Should 'fail with Rate limit with no http request'() {
        given:
        clock.millis() >> EPOCH.toEpochMilli()
        when: 'do 2nd request'
        client.findByRepoOwnerAndName('octokit', 'octokit.rb')
        then:
        def e = thrown(ResourceAccessException)
        assert e.cause.cause instanceof RateLimitException
        and: 'no new http requests'
        wireMock.allServeEvents.size() == 1
    }

    Should 'success http request in rate limit reset'() {
        given:
        def rateLimitResetStub = 'b23c9e41-62c6-40ac-a842-1b9e53d479e3'
        clock.millis() >> getRateLimitFromStubUUID(rateLimitResetStub)
        when: 'do 3nd request'
        def repoDesc = client.findByRepoOwnerAndName('octokit', 'octokit.rb')
        then:
        repoDesc.fullName == 'octokit/octokit.rb'
        and:
        wireMock.verify(2, getRequestedFor(urlEqualTo('/repos/octokit/octokit.rb')))
        wireMock.allServeEvents.size() == 2
    }

    static int getRateLimitFromStubUUID(String uuid) {
        wireMock.getStubMapping(fromString(uuid)).item.response.headers.getHeader('X-RateLimit-Reset').firstValue().toInteger()
    }

    @TestConfiguration
    static class TestConfig extends WiremockConfig {

        private final DetachedMockFactory factory = new DetachedMockFactory()

        @Bean
        Clock clock() {
            factory.Mock(Clock)
        }

        @Primary
        @Bean
        CloseableHttpClient ghHttpClient(Clock clock) {
            RateLimitCacheHttpClientBuilder
                    .create()
                    .setClock(clock)
                    .build()
        }

        @Override
        WireMockServer wireMock() {
            return wireMock
        }
    }

}