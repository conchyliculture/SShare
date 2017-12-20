package eu.renzokuken.sshare.upload;

/**
 * Created by renzokuken on 06/12/17.
 */

class SShareUploadException extends Exception {

    public String customMessage;

    SShareUploadException(String customMessage, Throwable cause) {
        super(cause);
        this.customMessage = customMessage;
    }

    public SShareUploadException(String customMessage) {
        super();
        this.customMessage = customMessage;
    }
}