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
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import net.schmizz.sshj.userauth.password.Resource;

import java.io.IOException;
import java.net.ConnectException;
import java.security.PublicKey;

import eu.renzokuken.sshare.ConnectionConstants;
import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.ui.AskPassphraseActivity;
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

    private String askPassphrase(String key) {
        Intent intent = new Intent(context, AskPassphraseActivity.class);
        intent.putExtra(context.getString(R.string.private_key_filename_handle), key);
        context.startActivity(intent);
        String result = AskPassphraseActivity.handler.getResponse();
        while (result == null) {
            try {
                Thread.sleep(200);
                result = AskPassphraseActivity.handler.getResponse();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return (result);
    }

    public void uploadFile(FileUri fileUri) throws SShareUploadException {
        monitor.notifyConnecting(connection.getHostString());
        SSHClient ssh = _Connect();
        if (ssh == null || !ssh.isConnected()) {
            // No SShareUploadException thrown, we most likely have said no to the host key
            monitor.notifyError(
                    context.getString(R.string.error_connection_failed, connection.getHostString()),
                    context.getString(R.string.error_user_did_not_recognize_key)
            );
            return;
        }
        monitor.notifyAuthenticating(connection.getHostString(), connection.username);
        _Authenticate(ssh);
        if (!ssh.isAuthenticated()) {
            throw new SShareUploadException(context.getString(R.string.error_login_failed));
        }
        monitor.notifyUploadStart();
        try {
            _Push(ssh, fileUri, connection.getRemotePath());
            _Cleanup(ssh);
        } catch (SShareUploadException e) {
            try {
                ssh.disconnect();
            } catch (IOException e1) {
                throw new SShareUploadException("Error closing ssh client", e1);
            }
            throw e;
        }
    }

    private SSHClient _Connect() throws SShareUploadException {
        SSHClient ssh = new SSHClient(new AndroidConfig());
        SshjHostKeyVerifier sshjHostKeyVerifier = new SshjHostKeyVerifier(context);
        switch (getHostKeyCheckingPref()) {
            case "NO":
                ssh.addHostKeyVerifier(new PromiscuousVerifier());
                break;
            default:
                ssh.addHostKeyVerifier(sshjHostKeyVerifier);
        }
        try {
            ssh.connect(connection.hostname, connection.port);
            return ssh;
        } catch (ConnectException e) {
            throw new SShareUploadException(
                    context.getString(R.string.error_connection_timeout, connection.getHostString()),
                    e);
        } catch (TransportException e) {
            if (e.getDisconnectReason().equals(DisconnectReason.HOST_KEY_NOT_VERIFIABLE)) {
                PublicKey key = sshjHostKeyVerifier.getLastSeenKey();
                boolean shouldAccept = askUser(
                        context.getString(R.string.unknown_host_key),
                        context.getString(R.string.warning_ask_fingerprint_trust, SecurityUtils.getFingerprint(key)));
                if (!shouldAccept) {
                    monitor.notifyError(
                            context.getString(R.string.upload_canceled),
                            context.getString(R.string.remote_host_key_not_accepted));
                    return ssh;
                }
                sshjHostKeyVerifier.addKey(connection, key);
                // Reconnect with the host key in the verifier
                return _Connect();
            } else {
                throw new SShareUploadException(context.getString(R.string.error_disconnected_from_host, connection.getHostString()), e);
            }
        } catch (Exception e) {
            throw new SShareUploadException(context.getString(R.string.error_connecting_to_host_with_msg, connection.getHostString()), e);
        }
    }

    private void _Authenticate(SSHClient ssh) throws SShareUploadException {
        AuthMethod authMethod = null;

        switch (ConnectionConstants.AuthenticationMethod.findByDbKey(connection.auth_mode)) {
            case ENUM_AUTH_KEY:
                try {
                    authMethod = new AuthPublickey(ssh.loadKeys(connection.key, new MyPasswordFinder(connection.key)));
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

    protected abstract void _Push(SSHClient ssh, FileUri fileUri, String destinationPath) throws SShareUploadException;

    private void _Cleanup(SSHClient ssh) throws SShareUploadException {
        try {
            ssh.disconnect();
            monitor.notifyFinish();
        } catch (IOException e) {
            throw new SShareUploadException(context.getString(R.string.error_cleanup), e);
        }
    }

    private class MyPasswordFinder implements PasswordFinder {

        private static final int MAX_NUM_TRIES = 3;
        private final String privateKeyFilename;
        private int numTries;

        MyPasswordFinder(String privateKeyFilename) {
            this.numTries = 0;
            this.privateKeyFilename = privateKeyFilename;
        }

        @Override
        public char[] reqPassword(Resource<?> resource) {
            String result = askPassphrase(this.privateKeyFilename);
            if (result.equals("")) {
                // CLicked on "cancel", so we bail out
                numTries+=MAX_NUM_TRIES;
            }
            numTries++;
            return result.toCharArray();
        }

        @Override
        public boolean shouldRetry(Resource<?> resource) {
            return numTries < MAX_NUM_TRIES;
        }
    }
}
