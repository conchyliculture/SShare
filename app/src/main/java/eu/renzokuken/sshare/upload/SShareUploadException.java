package eu.renzokuken.sshare.upload;

/**
 * Created by renzokuken on 06/12/17.
 */

class SShareUploadException extends Exception {

    public final String simpleMessage;
    public String detailsMessage = null;

    SShareUploadException(String customMessage, Throwable cause) {
        super(cause);
        this.simpleMessage = customMessage;
        this.detailsMessage = "(Caused by: " + cause.getLocalizedMessage() + ")";
    }

    public SShareUploadException(String simpleMessage, String detailsMessage) {
        super(simpleMessage);
        this.simpleMessage = simpleMessage;
        this.detailsMessage = detailsMessage;
    }

    public SShareUploadException(String customMessage) {
        super();
        this.simpleMessage = customMessage;
    }
}