package com.radius.feed.service;

import com.radius.feed.dto.CreatePostRequestDto;
import com.radius.feed.dto.PostDto;

import java.util.List;
import java.util.UUID;

public interface IFeedService {

    /**
     * Creates a new post on behalf of the authenticated Radius user.
     *
     * @param request  validated post payload (content + location)
     * @param userId   internal Radius user UUID, sourced from the
     *                 {@code X-Radius-User-Id} header injected by the gateway
     * @param userName Radius display username, sourced from the
     *                 {@code X-Radius-Username} header injected by the gateway
     */
    void createPost(CreatePostRequestDto request, UUID userId, String userName);

    List<PostDto> fetchAllPosts();

    List<PostDto> fetchNearbyPosts(Double latitude, Double longitude);
}
