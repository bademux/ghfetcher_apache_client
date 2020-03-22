package com.github.bademux.ghfetcher.controller;

import com.github.bademux.ghfetcher.service.GithubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GitHubRepoController {

    private final GithubService gitHubService;

    @GetMapping("/repositories/{owner}/{name}")
    ResponseEntity<GitHubRepo> search(@PathVariable String owner, @PathVariable String name) {
        log.debug("requested repo for owner {}, name {}", owner, name);
        return ResponseEntity.ok(gitHubService.findByRepoOwnerAndName(owner, name));
    }

}
