package eu.renzokuken.sshare.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import eu.renzokuken.sshare.R;

/**
 * Created by renzokuken on 13/12/17.
 */

public class PopupActivity extends Activity {

    public static final YesNoHandler handler = new YesNoHandler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String question = null;
        String title = getString(R.string.question_popup_title);

        if (getIntent().hasExtra(getString(R.string.question_handle))) {
            title = getIntent().getStringExtra(getString(R.string.title_handle));
            question = getIntent().getStringExtra(getString(R.string.message_handle));
        }

        if (question != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Message msg = Message.obtain();
                            msg.arg1 = 1;
                            handler.dispatchMessage(msg);
                            quit();
                        }
                    })
                    .setNegativeButton(getString(android.R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Message msg = Message.obtain();
                            msg.arg1 = 0;
                            handler.dispatchMessage(msg);
                            quit();
                        }
                    })
                    .create();
            alertDialog.setTitle(title);
            alertDialog.setMessage(question);
            alertDialog.setIcon(R.drawable.ic_info_black_24dp);
            alertDialog.show();
        }
    }

    private void quit() {
        finish();
    }

    public static class YesNoHandler extends Handler {

        private int response = -1;

        public int getResponse() {
            return response;
        }

        @Override
        public void handleMessage(Message msg) {
            this.response = msg.arg1;
        }
    }
}