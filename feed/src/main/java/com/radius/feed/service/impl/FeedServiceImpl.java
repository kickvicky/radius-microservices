package com.radius.feed.service.impl;

import com.radius.feed.dto.PostDto;
import com.radius.feed.entity.Post;
import com.radius.feed.exception.FeedServiceException;
import com.radius.feed.mapper.PostMapper;
import com.radius.feed.repository.PostRepository;
import com.radius.feed.service.IFeedService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class FeedServiceImpl implements IFeedService {

    private PostRepository postRepository;

    @Override
    public void createPost(PostDto postDto) {
        Post post = PostMapper.mapToPostEntity(postDto);
        post.setCreatedAt(LocalDateTime.now());
        postRepository.save(post);
    }

    @Override
    public List<PostDto> fetchAllPosts() {
        try {
            List<Post> posts = postRepository.findAll();
            return posts.stream()
                    .map(PostMapper::mapToPostDto)
                    .toList();
        } catch (Exception e) {
            throw new FeedServiceException("Error occurred while fetching posts: " + e.getMessage());
        }
    }
}
