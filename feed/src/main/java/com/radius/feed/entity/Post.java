package com.radius.feed.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.locationtech.jts.geom.Point;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;


    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 50)
    private String tag;

    /**
     * Geographic location stored as a PostGIS geography point (SRID 4326 = WGS84).
     * Use ST_DWithin for distance-based queries (returns meters with geography type).
     * JTS convention: coordinate order is (longitude, latitude).
     */
    @Column(nullable = false, columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Builder.Default
    @Column(name = "upvote_count")
    private Integer upvoteCount = 0;

    @Builder.Default
    @Column(name = "downvote_count")
    private Integer downvoteCount = 0;

    @Builder.Default
    @Column(name = "comment_count")
    private Integer commentCount = 0;

    @Builder.Default
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}

