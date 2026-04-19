package com.radius.feed.constant;

public final class FeedConstants {

    private FeedConstants() {
        // Prevent instantiation
    }

    public static final String STATUS_200 = "200";
    public static final String STATUS_201 = "201";
    public static final String STATUS_500 = "500";

    public static final String MESSAGE_POST_CREATED = "Post created successfully";

    // 15 miles converted to meters (1 mile = 1609.344 m)
    public static final double NEARBY_RADIUS_METERS = 15 * 1609.344; // 24140.16 m
}

