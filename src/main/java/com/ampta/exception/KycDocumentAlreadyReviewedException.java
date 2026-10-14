package com.ampta.exception;

public class KycDocumentAlreadyReviewedException extends RuntimeException {
    public KycDocumentAlreadyReviewedException(String message) {
        super(message);
    }
}
