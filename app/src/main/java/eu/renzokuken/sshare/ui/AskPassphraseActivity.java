package eu.renzokuken.sshare.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import eu.renzokuken.sshare.R;


/**
 * Created by renzokuken on 13/12/17.
 */


public class AskPassphraseActivity extends Activity {
    public static final AskPassphraseHandler handler = new AskPassphraseHandler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        String key = null;
        super.onCreate(savedInstanceState);
        if (getIntent().hasExtra(getString(R.string.private_key_filename_handle))) {
            key = getIntent().getStringExtra(getString(R.string.private_key_filename_handle));
        }
        @SuppressLint("InflateParams") final View view = getLayoutInflater().inflate(R.layout.passphrase_popup_layout, null);
        if (key != null) {
            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton(getString(android.R.string.yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Bundle b = new Bundle();
                            EditText et = view.findViewById(R.id.passphrase_editText);
                            b.putString("passphrase", et.getText().toString());
                            Message msg = Message.obtain();
                            msg.arg1 = 1;
                            msg.setData(b);
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
                    }).setCancelable(true)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            Message msg = Message.obtain();
                            msg.arg1 = 0;
                            handler.dispatchMessage(msg);
                            quit();
                        }
                    })
                    .create();
            alertDialog.setTitle(getString(R.string.ask_passphrase_title));
            alertDialog.setMessage(getString(R.string.passphrase_is_required, key));
            alertDialog.setIcon(R.drawable.ic_info_black_24dp);
            alertDialog.show();
        }
    }

    private void quit() {
        finish();
    }

    public static class AskPassphraseHandler extends Handler {

        private String response = null;

        public AskPassphraseHandler(Looper mainLooper) {
            super(mainLooper);
        }

        public String getResponse() {
            String newResponse = response;
            if (newResponse != null) {
                response = null;
            }
            return newResponse;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.arg1==1) {
                Bundle b = msg.getData();
                this.response = b.getString("passphrase");
            } else if (msg.arg1 == 0) {
                this.response = "";
            }
        }
    }
}