package com.c1se_01.roomiego.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * The type Response data.
 */
@Setter
@Getter
public class ResponseData {
    /**
     * Instantiates a new Response data.
     */
    public ResponseData() {
    }

    /**
     * Instantiates a new Response data.
     *
     * @param success the success
     * @param message the message
     * @param result  the result
     */
    public ResponseData(final boolean success, final String message, final Object result) {
        this.success = success;
        this.message = message;
        this.result = result;
    }

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private Object result;

}
