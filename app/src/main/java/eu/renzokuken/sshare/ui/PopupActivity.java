package eu.renzokuken.sshare.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import eu.renzokuken.sshare.R;

/**
 * Created by renzokuken on 13/12/17.
 */

public class PopupActivity extends Activity {

    private static final String TAG = "PopupActivity";
    public static final YesNoHandler handler = new YesNoHandler();

    public static class YesNoHandler extends Handler {

        private int response = -1;

        public int getResponse(){ return response; }

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "arg1 is "+msg.arg1+" what is "+msg.what);
            this.response = msg.arg1;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String question = null;
        if (getIntent().hasExtra("question")) {
            question = getIntent().getStringExtra("question");
        }

        if (question != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Message msg = Message.obtain();
                            msg.arg1 = 1;
                            handler.dispatchMessage(msg);
                            quit();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Message msg = Message.obtain();
                            msg.arg1 = 0;
                            handler.dispatchMessage(msg);
                            quit();
                        }
                    })
                    .create();
            alertDialog.setTitle("Question");
            alertDialog.setMessage(question);
            alertDialog.setIcon(R.drawable.ic_info_black_24dp);
            alertDialog.show();
        }
    }
    private void quit() {
        finish();
    }
}
