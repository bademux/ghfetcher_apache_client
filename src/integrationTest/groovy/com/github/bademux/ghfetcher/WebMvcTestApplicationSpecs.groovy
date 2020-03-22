package com.github.bademux.ghfetcher

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.env.Environment
import spock.lang.Specification

import java.lang.Object as Should

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE

@SpringBootTest(webEnvironment = NONE, properties = [
        'githubClient.rootUri=http://localhost',
        'githubClient.auth.type=basic',
        'githubClient.auth.basic.name=testName',
        'githubClient.auth.basic.password=testPass',
        'githubClient.cache.maxObjectSize=12',
        'githubClient.cache.maxEntries=1200',
        'githubClient.connection.maxTotal=40'
])
class WebMvcTestApplicationSpecs extends Specification {

    @Autowired
    Environment environment

    Should 'start up properly'() {
        expect: 'environment set up correctly'
        environment != null
    }

}