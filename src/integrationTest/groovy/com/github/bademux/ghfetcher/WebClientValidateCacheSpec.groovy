package com.github.bademux.ghfetcher

import com.github.bademux.ghfetcher.client.GithubClient
import com.github.bademux.ghfetcher.client.GithubClientConfig
import com.github.bademux.ghfetcher.utils.DateHeaderResponseTransformer
import com.github.bademux.ghfetcher.utils.RecordingWireMock
import com.github.bademux.ghfetcher.utils.WiremockConfig
import com.github.tomakehurst.wiremock.junit.WireMockClassRule
import org.junit.ClassRule
import org.junit.rules.RuleChain
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import spock.lang.Narrative
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

import java.lang.Object as Should
import java.time.Instant

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import static org.springframework.http.HttpHeaders.ACCEPT
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH

@Narrative("""
Here we test scenario where 1st call fetches an entity and 2nd call ensures the entity is in cache
""")
@ContextConfiguration(classes = [GithubClientConfig, SystemConfig, TestConfig])
@TestPropertySource(properties = [
        'githubClient.cache.maxObjectSize=12',
        'githubClient.cache.maxEntries=1200',
        'githubClient.connection.maxTotal=40'
])
@Stepwise
class WebClientValidateCacheSpec extends Specification {

    final static WireMockClassRule wireMock = new WireMockClassRule(wireMockConfig()
            .extensions(new DateHeaderResponseTransformer({ Instant.EPOCH }))
            .withRootDirectory("src/integrationTest/resources/${WebClientValidateCacheSpec.simpleName}")
            .dynamicPort()
    )

    @Shared
    @ClassRule
    RuleChain chain = RuleChain.outerRule(wireMock)
            .around(new RecordingWireMock(wireMock, 'https://api.github.com/'))

    @Autowired
    GithubClient client

    Should 'fetch data on first request'() {
        when:
        def repoDesc1 = client.findByRepoOwnerAndName('octokit', 'octokit.rb')
        then:
        repoDesc1.fullName == 'octokit/octokit.rb'
        and:
        wireMock.verify(1, getRequestedFor(urlEqualTo('/repos/octokit/octokit.rb'))
                .withoutHeader(IF_NONE_MATCH)
        )
        wireMock.allServeEvents.size() == 1
    }


    Should 'send ETag for 2nd request'() {
        when:
        def repoDesc2 = client.findByRepoOwnerAndName('octokit', 'octokit.rb')
        then: 'validate ETag'
        repoDesc2.fullName == 'octokit/octokit.rb'
        and:
        wireMock.verify(2, getRequestedFor(urlEqualTo('/repos/octokit/octokit.rb'))
                .withHeader(ACCEPT, equalTo('application/vnd.github.v3+json'))
        )
        wireMock.allServeEvents.size() == 2
    }

    @TestConfiguration
    static class TestConfig {

        @Primary
        @Bean
        PropertySourcesPlaceholderConfigurer props() {
            WiremockConfig.properties(wireMock)
        }

    }
}