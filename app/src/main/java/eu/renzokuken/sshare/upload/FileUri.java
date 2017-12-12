package eu.renzokuken.sshare.upload;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.Serializable;

/**
 * Created by renzokuken on 12/12/17.
 */

class FileUri implements Serializable {
    private final Context context;
    public String fileName = "-";
    public long fileSize = -1;
    @SuppressWarnings("unused")
    private long dateAdded;
    public long dateModified = -1;
    public Uri uri;

    /**
     * Locally store the file metadata.
     *
     */

    public FileUri(Context context, Uri fileUri) {
        this.context = context;
        loadFileMetadata(fileUri);
        dateAdded = -1;
    }

    private void loadFileMetadata(Uri fileUri) {
        this.uri = fileUri;
        Cursor returnCursor = context.getContentResolver().query(fileUri, null, null, null, null);
        if (returnCursor != null) {
            returnCursor.moveToFirst();

            this.fileName = returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            this.fileSize = Long.valueOf(returnCursor.getString(returnCursor.getColumnIndex(OpenableColumns.SIZE)));
            this.dateAdded = Long.valueOf(returnCursor.getString(returnCursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_ADDED)));
            this.dateModified = Long.valueOf(returnCursor.getString(returnCursor.getColumnIndex(MediaStore.Files.FileColumns.DATE_MODIFIED)));

            returnCursor.close();
        }
    }
}
