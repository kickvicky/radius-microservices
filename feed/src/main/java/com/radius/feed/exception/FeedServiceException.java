package com.radius.feed.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class FeedServiceException extends RuntimeException {

    public FeedServiceException(String message) {
        super(message);
    }
}

