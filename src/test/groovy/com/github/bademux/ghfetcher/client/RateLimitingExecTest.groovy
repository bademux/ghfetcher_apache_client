package com.github.bademux.ghfetcher.client

import org.apache.http.Header
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpRequestWrapper
import org.apache.http.impl.execchain.ClientExecChain
import org.apache.http.message.BasicHeader
import org.apache.http.message.BasicHttpResponse
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.Object as Should

import static java.time.Clock.fixed
import static java.time.Instant.EPOCH
import static java.time.ZoneOffset.UTC
import static org.apache.http.HttpVersion.HTTP_1_0
import static org.apache.http.client.methods.HttpRequestWrapper.wrap

class RateLimitingExecTest extends Specification {

    @Unroll
    Should "pass without triggering rateLimit for headers #headers"() {
        given:
        def expectedResponse = new BasicHttpResponse(HTTP_1_0, 200, 'all ok') as CloseableHttpResponse
        expectedResponse.setHeaders(headers.collect { k, v -> new BasicHeader(k, v as String) } as Header[])
        def requestExecutor = { rt, rq, ct, ex -> expectedResponse } as ClientExecChain
        def clock = fixed(EPOCH, UTC)
        def rateLimitingExec = new RateLimitingExec(clock, requestExecutor)
        HttpRequestWrapper request = wrap(new HttpGet('/test'))
        and: 'setup rate limit'
        rateLimitingExec.execute(null, request, null, null)
        when:
        def response = rateLimitingExec.execute(null, request, null, null)
        then:
        responseMsg == response.statusLine.reasonPhrase
        where:
        headers                                              || responseMsg
        []                                                   || 'all ok'
        ['X-RateLimit-Remaining': 1, 'X-RateLimit-Reset': 1] || 'all ok'
    }

    @Unroll
    Should "validate 'X-RateLimit-*' behaviour for headers #headers, expecting message #exMessage "() {
        given:
        def expectedResponse = new BasicHttpResponse(HTTP_1_0, 200, '') as CloseableHttpResponse
        expectedResponse.setHeaders(headers.collect { k, v -> new BasicHeader(k, v as String) } as Header[])
        def requestExecutor = { rt, rq, ct, ex -> expectedResponse } as ClientExecChain
        def clock = fixed(EPOCH, UTC)
        HttpRequestWrapper request = wrap(new HttpGet('/test'))
        def rateLimitingExec = new RateLimitingExec(clock, requestExecutor)
        and: 'setup rate limit'
        rateLimitingExec.execute(null, request, null, null)
        when:
        rateLimitingExec.execute(null, request, null, null)
        then:
        def e = thrown(RateLimitException)
        e.message == exMessage
        where:
        headers                                              || exMessage
        ['X-RateLimit-Remaining': 0, 'X-RateLimit-Reset': 1] || 'Rate limit reached, please wait till 1970-01-01T00:00:01Z'
        ['X-RateLimit-Remaining': 0, 'X-RateLimit-Reset': 2] || 'Rate limit reached, please wait till 1970-01-01T00:00:02Z'
        ['X-RateLimit-Reset': 1]                             || 'Rate limit reached, please wait till 1970-01-01T00:00:01Z'
    }


}
