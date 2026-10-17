package com.ampta.exception;

public class RejectionReasonRequiredException
        extends RuntimeException {

    public RejectionReasonRequiredException(String message) {
        super(message);
    }
}