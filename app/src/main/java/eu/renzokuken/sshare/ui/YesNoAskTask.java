package eu.renzokuken.sshare.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by renzokuken on 09/12/17.
 */

public class YesNoAskTask  extends UiTask<String, Boolean> {

    private final Context context;

    public YesNoAskTask(Context context) {
        this.context = context;
    }

    @Override
    protected void doOnUIThread(String... params) {
        new YesNoDialog(context, params[0]).show();
    }

    public class YesNoDialog extends AlertDialog.Builder {

        public YesNoDialog(Context ctx, String message) {
            super(ctx);
            setMessage(message);
            setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postResult(true);
                }
            });
            setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    postResult(false);
                }
            });
            setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    postResult(false);
                }
            });
        }

    }
}
