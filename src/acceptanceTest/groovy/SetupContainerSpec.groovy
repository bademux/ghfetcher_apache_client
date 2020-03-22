import com.github.bademux.ghfetcher.utils.RecordingWireMock
import com.github.tomakehurst.wiremock.junit.WireMockClassRule
import groovy.json.JsonSlurper
import org.junit.ClassRule
import org.junit.rules.RuleChain
import org.slf4j.LoggerFactory
import org.testcontainers.Testcontainers
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.Wait
import spock.lang.Shared
import spock.lang.Specification

import java.lang.Object as Should
import java.net.http.HttpClient
import java.net.http.HttpRequest

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import static java.net.URI.create
import static java.net.http.HttpResponse.BodyHandlers
import static java.util.Optional.ofNullable

class SetupContainerSpec extends Specification {

    static final WireMockClassRule wireMock
    static final GenericContainer container

    static {
        def log = LoggerFactory.getLogger(SetupContainerSpec)
        def dockerName = ofNullable(System.getenv().get('dockerName')).orElseThrow()
        def wireMockConfig = wireMockConfig()
                .withRootDirectory("src/acceptanceTest/resources/${SetupContainerSpec.simpleName}")
                .port(9090)
        wireMock = new WireMockClassRule(wireMockConfig)

        // wiremock  should be accessed from the container
        Testcontainers.exposeHostPorts(wireMockConfig.portNumber())

        container = new GenericContainer(dockerName)
                .withEnv('GITHUBCLIENT_AUTH_TYPE', 'basic')
                .withEnv('GITHUBCLIENT_AUTH_BASIC_NAME', 'bademux')
                .withEnv('GITHUBCLIENT_AUTH_BASIC_PASSWORD', 'XXX')
                .withEnv('GITHUBCLIENT_ROOTURI', "http://host.testcontainers.internal:${wireMockConfig.portNumber()}")
                .withEnv('LOGGING_LEVEL_org.springframework', 'DEBUG')
                .withExposedPorts(8080)
                .waitingFor(Wait.forHttp("/health"))
                .withLogConsumer(new Slf4jLogConsumer(log))
    }

    @Shared
    @ClassRule
    RuleChain chain = RuleChain.outerRule(wireMock)
            .around(new RecordingWireMock(wireMock, 'https://api.github.com/'))
            .around(container)


    def client = HttpClient.newHttpClient()

    def json = new JsonSlurper()

    Should 'do httpRequest request'() {
        given:
        HttpRequest request = HttpRequest.newBuilder()
                .uri(create("http://${getHost()}/repositories/octokit/octokit.rb"))
                .build()
        when:
        def response = client.send(request, BodyHandlers.ofString())
        then:
        response.statusCode() == 200
        and:
        json.parseText(response.body()) == [fullName   : 'octokit/octokit.rb',
                                            cloneUrl   : 'https://github.com/octokit/octokit.rb.git',
                                            description: 'Ruby toolkit for the GitHub API',
                                            stars      : 3135,
                                            createdAt  : '2009-12-10T21:41:49.000Z']
    }

    private static def getHost() {
        "${container.getContainerIpAddress()}:${container.getMappedPort(8080)}"
    }

}