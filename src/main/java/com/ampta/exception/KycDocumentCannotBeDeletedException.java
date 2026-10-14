package com.ampta.exception;

public class KycDocumentCannotBeDeletedException extends RuntimeException {
    public KycDocumentCannotBeDeletedException(String message) {
        super(message);
    }
}
