package com.ampta.repository;

public class KycDocumentAlreadyReviewedException extends RuntimeException {
    public KycDocumentAlreadyReviewedException(String message) {
        super(message);
    }
}
