package com.radius.feed.service.impl;

import com.radius.feed.constant.FeedConstants;
import com.radius.feed.dto.CreatePostRequestDto;
import com.radius.feed.dto.PostDto;
import com.radius.feed.entity.Post;
import com.radius.feed.exception.FeedServiceException;
import com.radius.feed.mapper.PostMapper;
import com.radius.feed.repository.PostRepository;
import com.radius.feed.service.IFeedService;
import lombok.AllArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class FeedServiceImpl implements IFeedService {

    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    private PostRepository postRepository;

    @Override
    public void createPost(CreatePostRequestDto request) {
        // Build the JTS Point from client-supplied coordinates (lon, lat order for JTS)
        Point location = GEOMETRY_FACTORY.createPoint(
                new Coordinate(request.getLongitude(), request.getLatitude())
        );

        Post post = Post.builder()
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000001"))  // TODO: replace with JWT principal
                .userName("anonymous")                                             // TODO: replace with JWT principal
                .content(request.getContent())
                .imageUrl(request.getImageUrl())
                .tag(request.getTag())
                .location(location)
                .upvoteCount(0)
                .downvoteCount(0)
                .commentCount(0)
                .isVerified(false)
                .build();

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

    @Override
    public List<PostDto> fetchNearbyPosts(Double latitude, Double longitude) {
        try {
            // JTS coordinate order: (x = longitude, y = latitude)
            Point userLocation = GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
            List<Post> posts = postRepository.findPostsWithinRadius(userLocation, FeedConstants.NEARBY_RADIUS_METERS);
            return posts.stream()
                    .map(PostMapper::mapToPostDto)
                    .toList();
        } catch (Exception e) {
            throw new FeedServiceException("Error occurred while fetching nearby posts: " + e.getMessage());
        }
    }
}
