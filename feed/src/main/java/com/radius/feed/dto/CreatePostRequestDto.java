package com.radius.feed.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Inbound request DTO for creating a post.
 * Clients only send content + location — all other fields are
 * either server-generated or mocked until auth is introduced.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequestDto {

    @NotBlank(message = "Content must not be empty")
    private String content;

    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0",  message = "Latitude must be >= -90")
    @DecimalMax(value = "90.0",   message = "Latitude must be <= 90")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
    @DecimalMax(value = "180.0",  message = "Longitude must be <= 180")
    private Double longitude;

    // Optional — a client may include these; null is fine for now
    private String imageUrl;
    private String tag;
}
