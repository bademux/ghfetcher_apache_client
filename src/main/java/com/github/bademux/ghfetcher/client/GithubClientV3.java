package com.github.bademux.ghfetcher.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.parseMediaType;

@RequiredArgsConstructor
@Slf4j
class GithubClientV3 implements GithubClient {

    private static final List<MediaType> ACCEPTABLE_MEDIA_TYPES = List.of(parseMediaType("application/vnd.github.v3+json"));
    private static final String USER_AGENT = "ghfecher/0.1";
    private static final HttpEntity DEFAULT_HEADERS = createDefaultHeaders();
    private static final String URI_VARIABLE_OWNER = "owner";
    private static final String URI_VARIABLE_NAME = "name";
    private static final String URL_REPO = String.format("/repos/{%s}/{%s}", URI_VARIABLE_OWNER, URI_VARIABLE_NAME);

    private final RestTemplate restTemplate;

    @Override
    public RepoDescription findByRepoOwnerAndName(String owner, String name) {
        ResponseEntity<RepoDescription> entity = restTemplate.exchange(URL_REPO, GET, DEFAULT_HEADERS,
                RepoDescription.class,
                createUriVariables(owner, name));
        return entity.getBody();
    }

    private Map<String, String> createUriVariables(String owner, String name) {
        return Map.of(URI_VARIABLE_OWNER, owner, URI_VARIABLE_NAME, name);
    }

    private static HttpEntity createDefaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(ACCEPTABLE_MEDIA_TYPES);
        headers.add(HttpHeaders.USER_AGENT, USER_AGENT);
        return new HttpEntity(headers);
    }

}
