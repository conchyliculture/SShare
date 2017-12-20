package eu.renzokuken.sshare.upload;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.security.Security;

import eu.renzokuken.sshare.ConnectionConstants;
import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;

/**
 * Created by renzokuken on 11/12/17.
 */

public class FileUploaderService extends IntentService {

    private static final String TAG = "FileUploaderService";

    static {
        // Need to remove outdated BouncyCastle Android crap, and insert SpongyCastle
        Security.removeProvider("BC");
        //Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    public FileUploaderService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Uri uri = intent.getExtras().getParcelable(getString(R.string.file_uri_handle));
        if (uri == null) {
            Log.d(TAG, "Error getting a null fileURI");
            return;
        }
        FileUri fileUri = new FileUri(this, uri);
        Connection connection = (Connection) intent.getExtras().getSerializable(getString(R.string.connection_handle));
        if (connection == null) {
            Log.d(TAG, "Error getting a null connection");
            return;
        }

        Monitor monitor = new Monitor(this, fileUri);

        switch (ConnectionConstants.ProtocolMethod.findByDbKey(connection.protocol)) {
            case ENUM_PROTO_SFTP:
                SftpFileUploaderSshj fileUploader = new SftpFileUploaderSshj(this, connection, monitor); // important d'avoir l'app context
                try {
                    fileUploader.uploadFile(fileUri);
                } catch (SShareUploadException e) {
                    monitor.error(e);
                }
                break;
            default:
                Log.e(TAG, "Protocol " + connection.protocol + " not implemented");
        }
    }
}
