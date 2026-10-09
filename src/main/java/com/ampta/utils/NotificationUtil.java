package com.ampta.utils;

public class NotificationUtil {
    private NotificationUtil() {
    }

    public static String kycUploadedMessage(String documentType) {
        return "New " + documentType +
                " document has been uploaded for verification.";
    }

    public static String kycApprovedMessage(String documentType) {
        return "Your " + documentType +
                " document has been approved.";
    }

    public static String kycRejectedMessage(
            String documentType,
            String reason) {

        return "Your " + documentType +
                " document has been rejected. Reason: " + reason;
    }

}
