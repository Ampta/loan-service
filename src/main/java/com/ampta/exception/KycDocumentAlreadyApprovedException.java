package com.ampta.exception;

public class KycDocumentAlreadyApprovedException
        extends RuntimeException {

    public KycDocumentAlreadyApprovedException(String message) {
        super(message);
    }
}