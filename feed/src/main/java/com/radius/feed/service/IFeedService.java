package com.radius.feed.service;

import com.radius.feed.dto.PostDto;

import java.util.List;

public interface IFeedService {

    void createPost(PostDto postDto);

    List<PostDto> fetchAllPosts();
}
