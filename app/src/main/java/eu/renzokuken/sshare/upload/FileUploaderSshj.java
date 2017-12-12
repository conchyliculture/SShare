package eu.renzokuken.sshare.upload;

import android.content.Context;

import android.preference.PreferenceManager;
import android.util.Log;

import net.schmizz.sshj.AndroidConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;

import java.io.IOException;
import java.net.ConnectException;
import java.security.PublicKey;

import eu.renzokuken.sshare.ConnectionHelpers;
import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;


/**
 * Created by renzokuken on 06/12/17.
 */

abstract class FileUploaderSshj {
    private static final String TAG =  "FileUploader";
    final Context context;
    final SSHClient ssh;
    private final SshjHostKeyVerifier sshjHostKeyVerifier;
    final Monitor monitor;

    FileUploaderSshj(Context context, Monitor monitor) {
        this.monitor = monitor;
        this.context = context;
        // Do specific android stuff
        ssh = new SSHClient(new AndroidConfig());
        sshjHostKeyVerifier = new SshjHostKeyVerifier(context);
        switch (getHostKeyCheckingPref()) {
            case "NO":
                ssh.addHostKeyVerifier(new PromiscuousVerifier());
                break;
            default:
                ssh.addHostKeyVerifier(sshjHostKeyVerifier);
        }
    }

    private String getHostKeyCheckingPref() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getString(R.string.pref_host_key_checking_mode),
                context.getString(R.string.pref_host_key_checking_mode_default));
    }

    private void _Connect(Connection connection) throws SShareUploadException {
        try {
            Log.d(TAG, "_Connect");
            ssh.connect(connection.hostname, connection.port);
        } catch (ConnectException e) {
            throw new SShareUploadException("Connection timeout to" + connection.getHostString() + "Check hostname & port", e);
        } catch (TransportException e) {

            if (e.getDisconnectReason().equals(DisconnectReason.HOST_KEY_NOT_VERIFIABLE)) {
                PublicKey key = sshjHostKeyVerifier.getLastSeenKey();
                /*boolean shouldAccept = new YesNoAskTask(context)
                        .execute("Unknown Host!\nDo you trust the following fingerprint:\n" + SecurityUtils.getFingerprint(key))
                        .get();*/
                boolean shouldAccept = true;  // TODO
                if (shouldAccept) {
                    sshjHostKeyVerifier.addKey(connection, key);
                    try {
                        ssh.connect(connection.hostname, connection.port);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    //_Connect(connection);
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
    }

    private void _Authenticate(Connection connection) throws SShareUploadException {
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
    
    private void _Cleanup() throws SShareUploadException {
        try {
            ssh.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            throw new SShareUploadException("Error during cleanup", e);
        }
        monitor.finish();
    }

    public void uploadFile(Connection connection, FileUri fileUri) throws SShareUploadException {
        _Connect(connection);
        _Authenticate(connection);
        if (!ssh.isConnected()) {
            throw new SShareUploadException("Could not connect");
        }
        _Push(connection, fileUri);
        _Cleanup();
    }

    protected abstract void _Push(Connection connection, FileUri fileUri) throws SShareUploadException;
}
