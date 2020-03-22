package com.github.bademux.ghfetcher.service;

import com.github.bademux.ghfetcher.client.GithubClient;
import com.github.bademux.ghfetcher.client.RepoDescription;
import com.github.bademux.ghfetcher.controller.GitHubRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class GithubService {

    private final GithubClient client;

    public GitHubRepo findByRepoOwnerAndName(String owner, String name) {
        return mapToResponse(client.findByRepoOwnerAndName(owner, name));
    }

    private GitHubRepo mapToResponse(RepoDescription entity) {
        return GitHubRepo.builder()
                .fullName(entity.getFullName())
                .description(entity.getDescription())
                .cloneUrl(entity.getCloneUrl())
                .stars(entity.getStargazersCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }

}
