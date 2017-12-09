package eu.renzokuken.sshare.upload;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import eu.renzokuken.sshare.persistence.Connection;


public class SFTPFileUploader extends FileUploader {
    private static final String TAG = "SFTPFileUploader";
    private JSch jsch = new JSch();
    private Session session = null;

    public SFTPFileUploader(Context context) {
        super(context);
    }

    private int getSFTPMode() {
        String val = getOverwritePref();

        switch (val) {
            case "0":
                return ChannelSftp.OVERWRITE;
            case "1":
                return ChannelSftp.RESUME;
        }
        return 0;
    }

    private void tryOpenSession(Connection connection, int maxRetry) {
        String username = connection.username;
        String host = connection.hostname;

        Log.d(TAG, "Starting SFTP Connection");
        try {
            session = jsch.getSession(username, host, connection.port);
            session.setPassword(connection.password);
            session.setHostKeyRepository(new MyHostKeyRepository(context));
            // jsch.setKnownHosts(khfile);
            // jsch.addIdentity(identityfile);

            session.setUserInfo(new MyUserInfo(context));
            Properties prop = new Properties();
            switch (getHostKeyCheckingPref()) {
                case "STRICT":
                    prop.put("StrictHostKeyChecking", "ask");
                    Log.d(TAG, "On ask!");
                    break;
                case "NO":
                    prop.put("StrictHostKeyChecking", "no");
                    Log.d(TAG, "No check");
                    break;
            }
            session.setConfig(prop);
            session.connect();
        } catch (JSchException jSchException) {
            HostKey hk = session.getHostKey();
            if (!(hk == null)) {
                Log.d(TAG, "HostKeyInfo: " +
                        hk.getHost() + " " +
                        hk.getType() + " " +
                        hk.getFingerPrint(jsch));
            } else {
                Log.d(TAG, "aww hk is null");
            }
        }
    }

    public void _uploadFile(Connection connection, Uri fileUri, SShareMonitor monitor) throws SShareUploadException {
        int maxRetry = 3;
        String destinationPath = getFilenameFromURI(fileUri);
        if (!(connection.remotePath == null || connection.remotePath.isEmpty())) {
            destinationPath = connection.remotePath + "/" + destinationPath;
        }


        try {
            tryOpenSession(connection, 3);

            if (! session.isConnected()) {
                throw new SShareUploadException("Couldn't open connection to " + connection);
            }

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            try {
                InputStream sourceStream = context.getContentResolver().openInputStream(fileUri);
                sftpChannel.put(sourceStream, destinationPath, monitor, getSFTPMode());
            } catch (FileNotFoundException e) {
                throw new SShareUploadException("Couldn't find file " + fileUri, e);
            } catch (SftpException e) {
                if (e.getMessage().equals("No such file")) {
                    String msg = "SFTP server said 'No such file', make sure the remote path is correct";
                    throw new SShareUploadException(msg, e);
                }
                throw new SShareUploadException("Error uploading " + fileUri, e);
            }

        } catch (JSchException e) {
            throw new SShareUploadException("Error connecting to " + connection.toString(), e);
        }
    }
}
