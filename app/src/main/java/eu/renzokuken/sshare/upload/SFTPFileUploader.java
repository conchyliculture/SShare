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
    private int sftpMode;


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

    public void _uploadFile(Connection connection, Uri fileUri, SShareMonitor monitor) throws SShareUploadException {
        String username = connection.username;
        String host = connection.hostname;
        String destinationPath = getFilenameFromURI(fileUri);
        if (!(connection.remotePath==null || connection.remotePath.isEmpty())) {
            destinationPath = connection.remotePath+"/"+destinationPath;
        }

        JSch jsch = new JSch();
        try {
            Log.d(TAG, "Starting SFTP Connection");
            Session session = jsch.getSession(username, host, connection.port);
            session.setPassword(connection.password);

            // jsch.setKnownHosts(khfile);
            // jsch.addIdentity(identityfile);


            try {
                session.connect();
            } catch (JSchException jSchException) {
                HostKey hk = session.getHostKey();
                if (! (hk == null)) {
                    Log.d(TAG, "HostKey: " +
                            hk.getHost() + " " +
                            hk.getType() + " " +
                            hk.getFingerPrint(jsch));
                } else {
                    Log.d(TAG, "aww hk is null");
                }
                switch (getHostKeyCheckingPref()) {
                    case "STRICT":
                        HostKeyRepository hkr = jsch.getHostKeyRepository();
                        break;
                    case "NO":
                        Properties prop = new Properties();
                        prop.put("StrictHostKeyChecking", "no");
                        session.setConfig(prop);
                        Log.d(TAG,"No check");
                        break;
                }
            }

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp)channel;
            try {
                InputStream sourceStream = context.getContentResolver().openInputStream(fileUri);
                sftpChannel.put(sourceStream, destinationPath, monitor, getSFTPMode());
            } catch (FileNotFoundException e) {
                throw new SShareUploadException("Couldn't find file "+fileUri, e);
            } catch (SftpException e) {
                throw new SShareUploadException("Error uploading "+fileUri, e);
            }

        } catch (JSchException e) {
            throw new SShareUploadException("Error connecting to "+connection.toString(), e);
        }
    }
}
