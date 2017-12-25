package eu.renzokuken.sshare.upload;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.util.Random;

import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.ui.MainActivity;

/**
 * Created by renzokuken on 12/12/17.
 */

class Monitor {
    private static final long NOTIFICATION_UPDATE_THROTTLE_MILLIS = 500;
    private final FileUri fileUri;
    private final Context context;
    private final NotificationManager notificationManager;
    private int notificationId = 1;
    private final NotificationCompat.Builder notificationBuilder;
    private long lastTick = System.currentTimeMillis();
    public boolean shouldStop = false;

    public Monitor(Context context, FileUri fileUri) {
        this.context = context;
        this.fileUri = fileUri;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_id));
        notificationBuilder.setSmallIcon(R.drawable.ic_file_upload_black_24dp);
        Random r = new Random();
        notificationId = r.nextInt(685463535); // lol

        Intent cancelIntent = new Intent(this.context, MainActivity.class);
        //TODO: make sure we need these flags, and maybe use another activity than MainActivity
        cancelIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        cancelIntent.setAction(context.getString(R.string.kill_uploads));
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context ,
                (int) System.currentTimeMillis(),
                cancelIntent,
                PendingIntent.FLAG_ONE_SHOT
        );
        notificationBuilder.addAction(R.drawable.ic_cancel_black_24dp, context.getString(R.string.cancel), pendingIntent);
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

    void error(final SShareUploadException e) {
        notifyError(e.simpleMessage, e.detailsMessage);
        String toastText = e.detailsMessage;
        if (e.getCause() != null) {
            toastText+= " (Caused by: " + e.getCause().getLocalizedMessage() + ")";
        }
        showToastInUiThread(context, toastText);
        e.printStackTrace();
    }

    public void notifyConnecting(String hostString) {
        String title = context.getString(R.string.connecting_to, hostString);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setProgress(0, 0, true);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void notifyAuthenticating(String hostString, String username) {
        String title = context.getString(R.string.authenticating_to, hostString);
        String subTitle = context.getString(R.string.as_user, username);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(subTitle);
        notificationBuilder.setProgress(0, 0, true);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    void notifyUploadStart() {
        String title = context.getString(R.string.uploading_filename, fileUri.fileName);
        notificationBuilder.setContentTitle(title);
        notificationBuilder.setContentText(null);
        notificationBuilder.setProgress(0, 0, true);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    void notifyProgress(long transferred) {
        // TODO: show ETA?
        long now = System.currentTimeMillis();
        if ((now - lastTick) < NOTIFICATION_UPDATE_THROTTLE_MILLIS) {
            // Try not to throw around too many notification updates
            return;
        }
        lastTick = now;
        int uploadedPercent = (int) ((100.0 * transferred) / fileUri.fileSize + 0.5);
        String percentString = String.format("%1$d%%", uploadedPercent);
        notificationBuilder.setContentTitle(context.getString(R.string.uploading_filename, fileUri.fileName))
                .setContentText(percentString)
                .setSmallIcon(R.drawable.ic_file_upload_black_24dp)
                .setOngoing(true)
                .setProgress(100, uploadedPercent, false);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void notifyFinish() {
        notificationBuilder.setProgress(0, 0, false)
                .setContentTitle(context.getString(R.string.upload_finished, fileUri.fileName))
                .setContentText(null)
                .setSmallIcon(R.drawable.ic_done_black_24dp)
                .setOngoing(false);
        notificationManager.notify(notificationId, notificationBuilder.build());
        // TODO: remove the notification after like 3 secs
    }
    void notifyError(String errorTitle, String errorSubText) {
        notificationBuilder.setContentTitle(errorTitle)
                .setOngoing(false)
                .setSmallIcon(R.drawable.ic_error_black_24dp)
                .setProgress(0, 0, false);
        if (errorSubText!=null) {
            notificationBuilder.setContentText(errorSubText);
        }
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
