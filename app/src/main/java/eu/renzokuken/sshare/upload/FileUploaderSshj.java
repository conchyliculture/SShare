package eu.renzokuken.sshare.upload;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

import net.schmizz.sshj.AndroidConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.DisconnectReason;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.UserAuthException;
import net.schmizz.sshj.userauth.method.AuthMethod;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.method.AuthPublickey;
import net.schmizz.sshj.userauth.password.PasswordUtils;

import java.io.IOException;
import java.net.ConnectException;
import java.security.PublicKey;

import eu.renzokuken.sshare.ConnectionConstants;
import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.ui.PopupActivity;


/**
 * Created by renzokuken on 06/12/17.
 */

abstract class FileUploaderSshj {
    final Context context;
    final Monitor monitor;
    private final Connection connection;

    FileUploaderSshj(Context context, Connection connection, Monitor monitor) {
        this.monitor = monitor;
        this.context = context;
        this.connection = connection;
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
            ssh.connect(connection.hostname, connection.port);
        } catch (ConnectException e) {
            throw new SShareUploadException(context.getString(R.string.error_connection_timeout, connection.getHostString()), e);
        } catch (TransportException e) {
            if (e.getDisconnectReason().equals(DisconnectReason.HOST_KEY_NOT_VERIFIABLE)) {
                PublicKey key = sshjHostKeyVerifier.getLastSeenKey();
                boolean shouldAccept = askUser(
                        context.getString(R.string.unknown_host_key),
                        context.getString(R.string.warning_ask_fingerprint_trust, SecurityUtils.getFingerprint(key)));
                if (shouldAccept) {
                    sshjHostKeyVerifier.addKey(connection, key);
                    ssh = _Connect();
                } else {
                    monitor.updateNotificationSubText(context.getString(R.string.remote_host_key_not_accepted));
                }
            } else {
                throw new SShareUploadException(context.getString(R.string.error_disconnected_from_host, connection.getHostString()), e);
            }
        } catch (Exception e) {
            throw new SShareUploadException(context.getString(R.string.error_connecting_to_host_with_msg, connection.getHostString(), e.getLocalizedMessage()), e);
        }
        return ssh;
    }

    private boolean askUser(String title, String question) {
        Intent intent = new Intent(context, PopupActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(context.getString(R.string.question_handle));
        intent.putExtra(context.getString(R.string.message_handle), question);
        intent.putExtra(context.getString(R.string.title_handle), title);

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
        monitor.updateNotificationSubText(context.getString(R.string.notification_authenticating_to, connection.getHostString()));
        AuthMethod authMethod = null;

        switch (ConnectionConstants.AuthenticationMethod.findByDbKey(connection.auth_mode)) {
            case ENUM_AUTH_KEY:
                try {
                    authMethod = new AuthPublickey(ssh.loadKeys(connection.key));
                } catch (IOException e) {
                    throw new SShareUploadException(context.getString(R.string.error_unable_to_load_key, connection.key), e);
                }
                break;
            case ENUM_AUTH_LP:
                authMethod = new AuthPassword(PasswordUtils.createOneOff(connection.password.toCharArray()));
                break;
        }
        try {
            ssh.auth(connection.username, authMethod);
        } catch (UserAuthException e) {
            throw new SShareUploadException(context.getString(R.string.error_login_failed), e);
        } catch (TransportException e) {
            throw new SShareUploadException(context.getString(R.string.error_transport), e);
        }
    }

    private void _Cleanup(SSHClient ssh) throws SShareUploadException {
        try {
            ssh.disconnect();
        } catch (IOException e) {
            throw new SShareUploadException(context.getString(R.string.error_cleanup), e);
        }
        monitor.finish();
    }

    public void uploadFile(FileUri fileUri) throws SShareUploadException {
        SSHClient ssh = _Connect();
        _Authenticate(ssh);
        if (!ssh.isConnected()) {
            throw new SShareUploadException(context.getString(R.string.error_could_not_connect));
        }
        _Push(ssh, fileUri, connection.remotePath);
        _Cleanup(ssh);
    }

    protected abstract void _Push(SSHClient ssh, FileUri fileUri, String destinationPath) throws SShareUploadException;
}
