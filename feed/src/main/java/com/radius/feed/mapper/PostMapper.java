package com.radius.feed.mapper;

import com.radius.feed.dto.PostDto;
import com.radius.feed.entity.Post;

public class PostMapper {

    public static PostDto mapToPostDto(Post post) {
        return PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .userName(post.getUserName())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .tag(post.getTag())
                .latitude(post.getLatitude())
                .longitude(post.getLongitude())
                .upvoteCount(post.getUpvoteCount())
                .downvoteCount(post.getDownvoteCount())
                .commentCount(post.getCommentCount())
                .isVerified(post.getIsVerified())
                .createdAt(post.getCreatedAt())
                .build();
    }

    public static Post mapToPostEntity(PostDto postDto) {
        return Post.builder()
                .id(postDto.getId())
                .userId(postDto.getUserId())
                .userName(postDto.getUserName())
                .content(postDto.getContent())
                .imageUrl(postDto.getImageUrl())
                .tag(postDto.getTag())
                .latitude(postDto.getLatitude())
                .longitude(postDto.getLongitude())
                .upvoteCount(postDto.getUpvoteCount())
                .downvoteCount(postDto.getDownvoteCount())
                .commentCount(postDto.getCommentCount())
                .isVerified(postDto.getIsVerified())
                .createdAt(postDto.getCreatedAt())
                .build();
    }
}
