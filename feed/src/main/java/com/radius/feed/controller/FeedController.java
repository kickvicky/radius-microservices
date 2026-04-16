package com.radius.feed.controller;

import com.radius.feed.constant.FeedConstants;
import com.radius.feed.dto.PostDto;
import com.radius.feed.dto.ResponseDto;
import com.radius.feed.service.IFeedService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class FeedController {

    private IFeedService feedService;

    @PostMapping("/post")
    public ResponseEntity<ResponseDto> createPost(@RequestBody PostDto postDto) {
        feedService.createPost(postDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(FeedConstants.STATUS_201, FeedConstants.MESSAGE_POST_CREATED));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostDto>> fetchAllPosts() {
        List<PostDto> posts = feedService.fetchAllPosts();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(posts);
    }

}
