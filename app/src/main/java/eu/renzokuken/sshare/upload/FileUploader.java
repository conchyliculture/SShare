package eu.renzokuken.sshare.upload;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import eu.renzokuken.sshare.persistence.Connection;

/**
 * Created by renzokuken on 06/12/17.
 */

public abstract class FileUploader {
    private static final String TAG = "FileUploader";
    final Context context;

    FileUploader(Context context) {
        this.context = context;
    }

    /**
     * Get the file's name URI from the fileUri.
     *
     * @return a String containing the filename.
     */
    String getFilenameFromURI(Uri fileUri) {
        String filename = "-";
        Cursor returnCursor = context.getContentResolver().query(fileUri, null, null, null, null);
        if (returnCursor != null) {
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            filename = returnCursor.getString(nameIndex);
            returnCursor.close();
        }
        return filename;
    }

    /**
     * Get the file's size URI from the fileUri.
     *
     * @return a long containing the file size in bytes.
     */
    private long getFileSizeFromURI(Uri fileUri) {
        long size = -1;
        Cursor returnCursor = context.getContentResolver().query(fileUri, null, null, null, null);
        if (returnCursor != null) {
            int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
            returnCursor.moveToFirst();
            size = returnCursor.getLong(sizeIndex);
            returnCursor.close();
        }
        return size;
    }

    protected abstract void _uploadFile(Connection connection, Uri fileUri, SShareMonitor monitor) throws SShareUploadException;

    public void uploadFile(final Connection connection, final Uri fileUri, final SShareMonitor monitor) {
        monitor.setFileSize(getFileSizeFromURI(fileUri));
        monitor.setFileName(getFilenameFromURI(fileUri));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    _uploadFile(connection, fileUri, monitor);
                } catch (SShareUploadException e) {
                    e.printStackTrace();
                    monitor.error(e.getMessage(), e);
                }
            }
        }).start();
        // TODO : return to previous activity from which we shared
    }
}
