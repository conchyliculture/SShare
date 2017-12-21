package eu.renzokuken.sshare.upload;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Random;

import eu.renzokuken.sshare.R;

/**
 * Created by renzokuken on 12/12/17.
 */

class Monitor {
    private static final String TAG = "SShareProgressMonitor";
    private static final long NOTIFICATION_UPDATE_THROTTLE_MILLIS = 500;
    private final FileUri fileUri;
    private final Context context;
    private final NotificationManager notificationManager;
    private int notificationId = 1;
    private final NotificationCompat.Builder notificationBuilder;
    private long lastTick = System.currentTimeMillis();

    public Monitor(Context context, FileUri fileUri) {
        this.context = context;
        this.fileUri = fileUri;
        // TODO: cancelable uploads
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id));
        notificationBuilder.setSmallIcon(R.drawable.ic_file_upload_black_24dp);
        if (notificationManager != null) {
            Random r = new Random();
            notificationId = r.nextInt(685463535); // lol
        } else {
            Log.e(TAG, "Notification manager is null =(");
        }
    }

    private static void showToastInUiThread(final Context context,
                                            final String message) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    void updateNotificationStart(String fileName) {
        notificationBuilder.setContentTitle(context.getString(R.string.uploading_filename, fileName))
                .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                .setOngoing(true)
                .setProgress(0, 0, true);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    void updateNotificationError(String text, String subText) {
        notificationBuilder.setContentTitle(text)
                .setContentText(subText)
                .setOngoing(false)
                .setSmallIcon(R.drawable.ic_error_black_24dp)
                .setProgress(0, 0, false);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    void updateNotificationTitleText(String message) {
        notificationBuilder.setContentTitle(message);
        notificationBuilder.setProgress(0, 0, true);
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

    public void updateNotificationFinish() {
        // Removes the progress bar
        notificationBuilder.setProgress(0, 0, false)
                .setContentTitle(context.getString(R.string.upload_finished, fileUri.fileName))
                .setSmallIcon(R.drawable.ic_done_black_24dp)
                .setOngoing(false);
        notificationManager.notify(notificationId, notificationBuilder.build());
        // TODO: remove the notification after like 3 secs
    }

    void error(final SShareUploadException e) {
        updateNotificationError(e.customMessage, e.getCause().getLocalizedMessage());
        String toastText = e.customMessage + " (" + e.getCause().getLocalizedMessage() + ")";
        showToastInUiThread(context, toastText);
        e.printStackTrace();
    }

    void progress(long transferred) {
        updateNotificationProgress(transferred);
    }
}
