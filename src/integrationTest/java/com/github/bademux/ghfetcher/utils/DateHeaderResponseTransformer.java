package com.github.bademux.ghfetcher.utils;

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.HttpHeader;
import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Supplier;

import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static java.time.ZoneOffset.UTC;
import static java.util.stream.Collectors.toList;

public final class DateHeaderResponseTransformer extends ResponseTransformer {

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(UTC);
    private static final String DATE = "Date";
    private final Supplier<Instant> instantProvider;

    public DateHeaderResponseTransformer(Supplier<Instant> instantProvider) {
        this.instantProvider = instantProvider;
    }

    @Override
    public Response transform(Request request, Response response, FileSource files, Parameters parameters) {
        return Response.Builder.like(response).but()
                .headers(replaceDateHeader(response.getHeaders()))
                .build();
    }

    private HttpHeaders replaceDateHeader(HttpHeaders httpHeaders) {
        List<HttpHeader> headers = httpHeaders.all().stream()
                .filter(header -> !header.keyEquals(DATE))
                .collect(toList());
        headers.add(httpHeader(DATE, DATE_TIME_FORMATTER.format(instantProvider.get())));
        return new HttpHeaders(headers);
    }

    @Override
    public String getName() {
        return "global-date-response-transformer";
    }

    @Override
    public boolean applyGlobally() {
        return true;
    }

}