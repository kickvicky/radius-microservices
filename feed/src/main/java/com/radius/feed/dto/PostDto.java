package com.radius.feed.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {

    private UUID id;
    private UUID userId;
    private String userName;
    private String content;
    private String imageUrl;
    private String tag;
    private Double latitude;
    private Double longitude;
    private Integer upvoteCount;
    private Integer downvoteCount;
    private Integer commentCount;
    private Boolean isVerified;
    private LocalDateTime createdAt;

}

