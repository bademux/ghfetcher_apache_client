package com.github.bademux.ghfetcher.controller;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class GitHubRepo {

    static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss[.SSS]X"; // hackfix fo java8 DateTime formatter,

    @JsonProperty("fullName")
    private String fullName;
    @JsonProperty("cloneUrl")
    private String cloneUrl;
    @JsonProperty("description")
    private String description;
    @JsonProperty("stars")
    private int stars;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = ISO8601)
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

}