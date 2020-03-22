package com.github.bademux.ghfetcher.client;

import org.apache.http.impl.client.cache.CachingHttpClientBuilder;
import org.apache.http.impl.execchain.ClientExecChain;

import java.time.Clock;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class RateLimitCacheHttpClientBuilder extends CachingHttpClientBuilder {

    private Clock clock;

    public static RateLimitCacheHttpClientBuilder create() {
        return new RateLimitCacheHttpClientBuilder();
    }

    public final CachingHttpClientBuilder setClock(Clock clock) {
        this.clock = requireNonNull(clock);
        return this;
    }

    /**
     * apply  RateLimitingExec after CachingExec
     *
     * @param mainExec - CachingExec
     * @return
     */
    @Override
    protected ClientExecChain decorateMainExec(ClientExecChain mainExec) {
        return super.decorateMainExec(new RateLimitingExec(getClockOrDefault(), mainExec));
    }

    private Clock getClockOrDefault() {
        return ofNullable(clock).orElseGet(Clock::systemDefaultZone);
    }

}