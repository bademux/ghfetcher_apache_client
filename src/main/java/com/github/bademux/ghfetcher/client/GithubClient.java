package com.github.bademux.ghfetcher.client;

public interface GithubClient {

    RepoDescription findByRepoOwnerAndName(String owner, String name);

}
