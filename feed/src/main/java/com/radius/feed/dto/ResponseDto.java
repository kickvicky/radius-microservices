package com.radius.feed.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDto {

    private String statusCode;
    private String statusMessage;

}

