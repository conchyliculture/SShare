package eu.renzokuken.sshare.upload;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.security.Security;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import eu.renzokuken.sshare.ConnectionConstants;
import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;

/**
 * Created by renzokuken on 11/12/17.
 */

public class FileUploaderService extends Service {

    private static final String TAG = "FileUploaderService";

    static {
        // Need to remove outdated BouncyCastle Android crap, and insert SpongyCastle
        Security.removeProvider("BC");
        //Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
    }

    private ExecutorService executorService;
    private final ArrayList<Monitor> monitorList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        // let's create a thread pool with five threads
        executorService = Executors.newFixedThreadPool(5);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class UploadRunnable extends Thread {
        private final FileUploaderSshj uploader;
        private final Monitor monitor;
        private final FileUri fileUri;

        public UploadRunnable(FileUploaderSshj uploader, FileUri fileUri, Monitor monitor) {
            this.fileUri = fileUri;
            this.uploader = uploader;
            this.monitor = monitor;
        }

        @Override
        public void run() {
            try {
                uploader.uploadFile(fileUri);
            } catch (SShareUploadException e) {
                monitor.error(e);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action != null) {
            if (action.equals(getString(R.string.new_upload_action))) {
                newUpload(intent);
            } else if (action.equals(getString(R.string.kill_upload_action))) {
                cancelUpload(intent);
            }
        }
        return START_NOT_STICKY; // TOdo check this
    }

    private void cancelUpload(Intent intent) {
        if (intent.hasExtra(getString(R.string.notification_handle))) {
            int notificationId = intent.getIntExtra(getString(R.string.notification_handle), -1);
            for (Monitor monitor : monitorList) {
                if (monitor.notificationId == notificationId) {
                    monitor.shouldStop = true;
                }
            }
        }
    }

    private void newUpload(Intent intent) {
        Uri uri = intent.getData();
        if (uri == null) {
            Log.e(TAG, "Error getting a null fileURI");
            return;
        }
        Connection connection = null;
        if (intent.hasExtra(getString(R.string.connection_handle))) {
            connection = (Connection) intent.getSerializableExtra(getString(R.string.connection_handle));
        }
        if (connection == null) {
            Log.e(TAG, "Error getting a null connection");
            return;
        }

        FileUri fileUri = new FileUri(this, uri);
        Monitor monitor = new Monitor(getApplicationContext(), fileUri);
        FileUploaderSshj fileUploader;
        switch (ConnectionConstants.ProtocolMethod.findByDbKey(connection.protocol)) {
            case ENUM_PROTO_SFTP:
                fileUploader = new SftpFileUploaderSshj(getApplicationContext(), connection, monitor); // important d'avoir l'app context
                break;
            default:
                // Shouldn't happen anyway
                monitor.notifyError("Protocol " + connection.protocol + " not implemented", null);
                return;
        }
        UploadRunnable task = new UploadRunnable(fileUploader, fileUri, monitor);
        monitorList.add(monitor);
        executorService.submit(task);


    }
}
