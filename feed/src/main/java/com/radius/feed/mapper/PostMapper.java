package com.radius.feed.mapper;

import com.radius.feed.dto.PostDto;
import com.radius.feed.entity.Post;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class PostMapper {

    // SRID 4326 = WGS84 (standard GPS / lat-lon coordinate system)
    private static final GeometryFactory GEOMETRY_FACTORY =
            new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * Creates a PostGIS-compatible Point from latitude and longitude.
     * JTS coordinate order is (x=longitude, y=latitude).
     */
    private static Point toPoint(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) return null;
        return GEOMETRY_FACTORY.createPoint(new Coordinate(longitude, latitude));
    }

    public static PostDto mapToPostDto(Post post) {
        Point location = post.getLocation();
        return PostDto.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .userName(post.getUserName())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .tag(post.getTag())
                // Extract lat/lon back from the Point (x=lon, y=lat)
                .latitude(location != null ? location.getY() : null)
                .longitude(location != null ? location.getX() : null)
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
                .location(toPoint(postDto.getLatitude(), postDto.getLongitude()))
                .upvoteCount(postDto.getUpvoteCount())
                .downvoteCount(postDto.getDownvoteCount())
                .commentCount(postDto.getCommentCount())
                .isVerified(postDto.getIsVerified())
                .createdAt(postDto.getCreatedAt())
                .build();
    }
}
