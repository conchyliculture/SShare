package eu.renzokuken.sshare.upload;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.jcraft.jsch.SftpProgressMonitor;

import java.util.Random;

import eu.renzokuken.sshare.R;

/**
 * Created by renzokuken on 06/12/17.
 */


public class SShareMonitor implements SftpProgressMonitor {
    private static final String TAG = "SShareProgressMonitor";
    private static final long NOTIFICATION_UPDATE_THROTTLE_MILLIS = 500;
    private int notificationId = 1;
    private NotificationCompat.Builder notificationBuilder;
    private final NotificationManager notificationManager;
    private long totalUploaded = 0;
    private String fileName = "-";
    private long fileSize = -1;

    private long lastTick = System.currentTimeMillis();

    public SShareMonitor(Context context) {
        // TODO: cancelable uploads
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            Random r = new Random();
            notificationId = r.nextInt(685463535); // lol
            notificationBuilder = new NotificationCompat.Builder(context, context.getString(R.string.notification_channelid));
            notificationBuilder.setContentTitle("File Upload (SShare)")
                    .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                    .setContentText("Preparing connection");
            notificationManager.notify(notificationId, notificationBuilder.build());
        } else {
            Log.e(TAG, "Notification manager is null =(");
        }
    }

    void setFileSize(long size) {
        fileSize = size;
    }

    void setFileName(String name) {
        fileName = name;
    }

    private void updateNotificationSubText(String message) {


        notificationBuilder.setContentText(message);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    private void updateNotificationProgressIncrementally(long uploadedData) {
        if (fileSize == -1) {
            // we don't know the total size, bailing out
            return;
        }
        totalUploaded = totalUploaded + uploadedData;
        // TODO: show ETA?
        long now = System.currentTimeMillis();
        if ((now - lastTick) < NOTIFICATION_UPDATE_THROTTLE_MILLIS) {
            // Try not to throw around too many notification updates
            return;
        }
        lastTick = now;
        int uploadedPercent = (int) ((100.0 * totalUploaded) / fileSize + 0.5);
        notificationBuilder.setProgress(100, uploadedPercent, false);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    @Override
    public void init(int op, String src, String dest, long max) {
    }

    @Override
    public boolean count(long uploadedData) {
        updateNotificationProgressIncrementally(uploadedData);
        return true;
    }

    @Override
    public void end() {
        // Removes the progress bar
        notificationBuilder.setProgress(0, 0, false);
        notificationManager.notify(notificationId, notificationBuilder.build());

        String message = "Upload finished";
        if (!fileName.equals("")) {
            message = "Upload finished (" + fileName + ")";
        }
        updateNotificationSubText(message);
        // TODO: remove the notification after like 3 secs
    }

    void error(String message, Throwable e) {
        updateNotificationSubText(message);
        Log.e(TAG, "Got error " + message);
        e.printStackTrace();
    }
}