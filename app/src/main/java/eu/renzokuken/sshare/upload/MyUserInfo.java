package eu.renzokuken.sshare.upload;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;

import com.jcraft.jsch.UserInfo;

import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.ui.YesNoAskTask;

/**
 * Created by renzokuken on 08/12/17.
 */

public class MyUserInfo implements UserInfo {

    private static final String TAG = "MyUserInfo" ;
    private final Context context;
    private String value = "";

    public MyUserInfo(Context context) {
        this.context = context;
    }

    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean promptPassword(String message) {
        return false;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return false;
    }

    private Thread askYesNo(String message) {
        final String msg = message;

        Thread t = new Thread() {
                public void run() {
//                    Looper.prepare();
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(context, R.style.YesNoDialog);
                    builder.setTitle("Dialog");
                    builder.setMessage(msg);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            value = "true";
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            value = "false";
                        }
                    });
                    builder.show();
                    try {
                        while(value.equals("")) {
                            Log.d(TAG, "vazy répond");
                            Thread.sleep(500);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(TAG, "Fin du thread");
                }
        };
        return t;
    }

    @Override
    public boolean promptYesNo(String message) {
        try {
            return new YesNoAskTask(context)
                    .execute(message)
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void showMessage(String message) {

    }
}
