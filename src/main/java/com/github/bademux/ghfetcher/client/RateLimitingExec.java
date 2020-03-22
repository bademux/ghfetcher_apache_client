package com.github.bademux.ghfetcher.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.execchain.ClientExecChain;

import java.io.IOException;
import java.time.Clock;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static java.time.Instant.ofEpochSecond;
import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Slf4j
public class RateLimitingExec implements ClientExecChain {

    /**
     * The maximum number of requests you're permitted to make per hour.
     */
    private static final String HEADER_RATE_LIMIT = "X-RateLimit-Limit";
    /**
     * The number of requests remaining in the current rate limit window.
     */
    private static final String HEADER_RATE_REMAINING = "X-RateLimit-Remaining";
    /**
     * The time at which the current rate limit window resets in UTC epoch seconds.
     */
    private static final String HEADER_RATE_RESET = "X-RateLimit-Reset";

    private static final int NO_RATE_LIMIT = -1;

    private final AtomicLong rateLimitReset = new AtomicLong(NO_RATE_LIMIT);

    private final Clock clock;
    private final ClientExecChain requestExecutor;


    @Override
    public CloseableHttpResponse execute(HttpRoute route, HttpRequestWrapper request, HttpClientContext context,
                                         HttpExecutionAware execAware) throws IOException, HttpException {
        checkIsRateLimited();
        CloseableHttpResponse response = requestExecutor.execute(route, request, context, execAware);
        handleRateLimitedResponse(response);
        return response;
    }

    private void checkIsRateLimited() throws RateLimitException {
        long rateLimitReset = this.rateLimitReset.get();
        if (NO_RATE_LIMIT == rateLimitReset) {
            return;
        }
        if (rateLimitReset >= getCurrentEpoch()) {
            throw new RateLimitException("Rate limit reached, please wait till " + ofEpochSecond(rateLimitReset).toString());
        }
        this.rateLimitReset.set(NO_RATE_LIMIT);
    }


    private long getCurrentEpoch() {
        return clock.millis();
    }

    private void handleRateLimitedResponse(HttpResponse response) {
        logDebugRateInfo(response);
        if (getRemaining(response) < 1) {
            getHeaderByName(response, HEADER_RATE_RESET)
                    .map(Long::valueOf)
                    .ifPresent(rateLimitReset::set);
        }
    }

    private long getRemaining(HttpResponse response) {
        return getHeaderByName(response, HEADER_RATE_REMAINING)
                .map(Long::valueOf)
                .orElse(0L);
    }

    private void logDebugRateInfo(HttpResponse response) {
        if (log.isDebugEnabled()) {
            log.debug("Remains '{}/{}'. Till counter resets at '{}'",
                    getHeaderByName(response, HEADER_RATE_REMAINING).orElse("none"),
                    getHeaderByName(response, HEADER_RATE_LIMIT).orElse("none"),
                    getHeaderByName(response, HEADER_RATE_RESET).orElse("none"));
        }
    }

    private Optional<String> getHeaderByName(HttpResponse response, String headerName) {
        return ofNullable(response.getFirstHeader(headerName))
                .map(NameValuePair::getValue);
    }


}
