package com.github.bademux.ghfetcher.client;

import org.apache.http.ProtocolException;

public class RateLimitException extends ProtocolException {

    private static final long serialVersionUID = -4984203307961553229L;

    public RateLimitException() {
        super();
    }

    public RateLimitException(final String message) {
        super(message);
    }

    public RateLimitException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
