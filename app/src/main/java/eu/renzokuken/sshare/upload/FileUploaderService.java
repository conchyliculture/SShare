package eu.renzokuken.sshare.upload;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import eu.renzokuken.sshare.ConnectionHelpers;
import eu.renzokuken.sshare.persistence.Connection;

/**
 * Created by renzokuken on 11/12/17.
 */

public class FileUploaderService extends IntentService {

    private static final String TAG = "FileUploaderService";

    public FileUploaderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Uri uri = intent.getExtras().getParcelable("fileURI");
        if (uri == null) {
            Log.d(TAG, "Error getting a null fileURI");
            return;
        }
        FileUri fileUri = new FileUri(this, uri);
        Connection connection = (Connection) intent.getExtras().getSerializable("connection");
        if (connection == null) {
            Log.d(TAG, "Error getting a null connection");
            return;
        }

        Monitor monitor = new Monitor(this, fileUri);

        if (connection.protocol.equals(ConnectionHelpers.MODE_SFTP)) {
            SftpFileUploaderSshj fileUploader = new SftpFileUploaderSshj(this, monitor); // important d'avoir l'app context
            try {
                fileUploader.uploadFile(connection, fileUri);
            } catch (SShareUploadException e) {
                e.printStackTrace();
                monitor.error(e.getMessage(), e);
            }
        } else {
            Log.e(TAG, "Protocol " + connection.protocol + " not implemented =(");
        }
    }
}
