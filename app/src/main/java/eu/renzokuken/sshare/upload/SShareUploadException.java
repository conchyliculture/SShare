package eu.renzokuken.sshare.upload;

/**
 * Created by renzokuken on 06/12/17.
 */

class SShareUploadException extends Exception {
    SShareUploadException(String message, Throwable cause) {
        super(message, cause);
    }

    public SShareUploadException(String message) {
        super(message);
    }
}