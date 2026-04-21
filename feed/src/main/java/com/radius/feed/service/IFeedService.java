package com.radius.feed.service;

import com.radius.feed.dto.CreatePostRequestDto;
import com.radius.feed.dto.PostDto;

import java.util.List;

public interface IFeedService {

    void createPost(CreatePostRequestDto request);

    List<PostDto> fetchAllPosts();

    List<PostDto> fetchNearbyPosts(Double latitude, Double longitude);
}
