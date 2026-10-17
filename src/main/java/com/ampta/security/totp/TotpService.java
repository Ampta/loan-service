package com.ampta.security.totp;

public interface TotpService {
    String generateSecret();
    String getQrCodeDataUri(String secret, String email);
    boolean verifyCode(String secret, String code);
}
