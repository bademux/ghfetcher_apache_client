package com.github.bademux.ghfetcher.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
public class RepoDescription {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("description")
    private String description;
    @JsonProperty("clone_url")
    private String cloneUrl;
    @JsonProperty("stargazers_count")
    private Integer stargazersCount;
    @JsonProperty("created_at")
    private OffsetDateTime createdAt;

}