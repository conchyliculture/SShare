package eu.renzokuken.sshare.upload;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Random;

import eu.renzokuken.sshare.R;

/**
 * Created by renzokuken on 12/12/17.
 */

class Monitor {
    private static final String TAG = "SShareProgressMonitor";
    private static final long NOTIFICATION_UPDATE_THROTTLE_MILLIS = 500;
    private final FileUri fileUri;
    private int notificationId = 1;
    private NotificationCompat.Builder notificationBuilder;
    private final NotificationManager notificationManager;

    private long lastTick = System.currentTimeMillis();

    public Monitor(Context context, FileUri fileUri) {
        this.fileUri = fileUri;
        // TODO: cancelable uploads
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            Random r = new Random();
            notificationId = r.nextInt(685463535); // lol
            notificationBuilder = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id));
            notificationBuilder.setContentTitle("Uploading '" + fileUri.fileName + "' (SShare)")
                    .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                    .setContentText("Preparing connection");
            notificationManager.notify(notificationId, notificationBuilder.build());
        } else {
            Log.e(TAG, "Notification manager is null =(");
        }
    }

    void updateNotificationSubText(String message) {
        notificationBuilder.setContentText(message);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void updateNotificationProgress(long uploadedData) {
        // TODO: show ETA?
        long now = System.currentTimeMillis();
        if ((now - lastTick) < NOTIFICATION_UPDATE_THROTTLE_MILLIS) {
            // Try not to throw around too many notification updates
            return;
        }
        lastTick = now;
        int uploadedPercent = (int) ((100.0 * uploadedData) / fileUri.fileSize + 0.5);
        notificationBuilder.setProgress(100, uploadedPercent, false);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void finish() {
        // Removes the progress bar
        notificationBuilder.setProgress(0, 0, false);
        notificationManager.notify(notificationId, notificationBuilder.build());

        String message = "Finished uploading "+fileUri.fileName;
        updateNotificationSubText(message);
        // TODO: remove the notification after like 3 secs
    }

    void error(String message, Throwable e) {
        updateNotificationSubText(message);
        Log.e(TAG, "Got error " + message);
        e.printStackTrace();
    }

    void progress(long transferred) {
        updateNotificationProgress(transferred);
    }
}
