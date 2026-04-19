package com.radius.feed.controller;

import com.radius.feed.constant.FeedConstants;
import com.radius.feed.dto.PostDto;
import com.radius.feed.dto.ResponseDto;
import com.radius.feed.service.IFeedService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class FeedController {

    private static final Logger log = LoggerFactory.getLogger(FeedController.class);

    private IFeedService feedService;

    @PostMapping("/post")
    public ResponseEntity<ResponseDto> createPost(@RequestBody PostDto postDto) {
        feedService.createPost(postDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(FeedConstants.STATUS_201, FeedConstants.MESSAGE_POST_CREATED));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostDto>> fetchPosts(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng) {

        List<PostDto> posts;
        if (lat != null && lng != null) {
            log.info("[FeedController] fetchNearbyPosts triggered → lat={}, lng={}, radius=15 miles", lat, lng);
            posts = feedService.fetchNearbyPosts(lat, lng);
        } else {
            log.info("[FeedController] fetchAllPosts triggered → no lat/lng provided");
            posts = feedService.fetchAllPosts();
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(posts);
    }

}
