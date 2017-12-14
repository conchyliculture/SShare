package eu.renzokuken.sshare.upload;

import android.content.Context;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

import net.schmizz.sshj.AndroidConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;

import java.io.IOException;
import java.net.ConnectException;
import java.security.PublicKey;

import eu.renzokuken.sshare.ConnectionHelpers;
import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.ui.PopupActivity;


/**
 * Created by renzokuken on 06/12/17.
 */

abstract class FileUploaderSshj {
    private static final String TAG =  "FileUploader";
    final Context context;
    final Monitor monitor;
    private final Connection connection;

    FileUploaderSshj(Context context, Connection connection, Monitor monitor) {
        this.monitor = monitor;
        this.context = context;
        this.connection = connection;
        // Do specific android stuff

    }

    private String getHostKeyCheckingPref() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.pref_host_key_checking_mode),
                context.getString(R.string.pref_host_key_checking_mode_default));
    }

    private SSHClient _Connect() throws SShareUploadException {
        SSHClient ssh = new SSHClient(new AndroidConfig());
        SshjHostKeyVerifier sshjHostKeyVerifier = new SshjHostKeyVerifier(context);
        try {
            switch (getHostKeyCheckingPref()) {
                case "NO":
                    ssh.addHostKeyVerifier(new PromiscuousVerifier());
                    break;
                default:
                    ssh.addHostKeyVerifier(sshjHostKeyVerifier);
            }
            Log.d(TAG, "_Connect "+ssh);
            ssh.connect(connection.hostname, connection.port);
        } catch (ConnectException e) {
            throw new SShareUploadException("Connection timeout to" + connection.getHostString() + "Check hostname & port", e);
        } catch (TransportException e) {
            if (e.getDisconnectReason().equals(DisconnectReason.HOST_KEY_NOT_VERIFIABLE)) {
                PublicKey key = sshjHostKeyVerifier.getLastSeenKey();
                boolean shouldAccept = askUser("Unknown Host!\nDo you trust the following fingerprint:\n" + SecurityUtils.getFingerprint(key));
                if (shouldAccept) {
                    sshjHostKeyVerifier.addKey(connection, key);
                    ssh = _Connect();
                } else {
                    throw new SShareUploadException("Didn't accept key", e);
                }
            } else {
                e.printStackTrace();
                throw new SShareUploadException("Error connecting to " + connection.getHostString(), e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ssh;
    }

    private boolean askUser(String question) {
        Log.d(TAG, "Asking "+question);

        Intent intent = new Intent(context, PopupActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("question");
        intent.putExtra("question", question);

        context.startActivity(intent);

        PopupActivity.YesNoHandler handler = PopupActivity.handler;

        while (handler.getResponse() == -1) {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return (handler.getResponse() == 1);
    }

    private void _Authenticate(SSHClient ssh) throws SShareUploadException {
        monitor.updateNotificationSubText("Authenticating to " + connection.toString());
        try {
            switch (connection.auth_mode) {
                case ConnectionHelpers.AUTH_KEY:
                    // TODO
                    Log.d(TAG, "Public key auth not implemented");
                    break;
                case ConnectionHelpers.AUTH_LP:
                    ssh.authPassword(connection.username, connection.password);
                    break;
            }
        } catch (UserAuthException e) {
            e.printStackTrace();
            throw new SShareUploadException("Could not login. Check your credentials", e);
        } catch (TransportException e) {
            e.printStackTrace();
        }
    }
    
    private void _Cleanup(SSHClient ssh) throws SShareUploadException {
        try {
            ssh.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SShareUploadException("Error during cleanup", e);
        }
        monitor.finish();
    }

    public void uploadFile(FileUri fileUri) throws SShareUploadException {
        SSHClient ssh = _Connect();
        _Authenticate(ssh);
        if (!ssh.isConnected()) {
            throw new SShareUploadException("Could not connect");
        }
        _Push(ssh, fileUri);
        _Cleanup(ssh);
    }

    protected abstract void _Push(SSHClient ssh, FileUri fileUri) throws SShareUploadException;


}
