package eu.renzokuken.sshare.upload;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import eu.renzokuken.sshare.persistence.Connection;

import static com.jcraft.jsch.ChannelSftp.OVERWRITE;

public class SFTPFileUploader extends FileUploader {
    private static final String TAG = "SFTPFileUploader";
    private int sftpMode = OVERWRITE; // TODO

    public SFTPFileUploader(Context context) {
        super(context);
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

            Properties prop = new Properties();
            prop.put("StrictHostKeyChecking", "no"); // TODO
            session.setConfig(prop);

            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp)channel;
            try {
                InputStream sourceStream = context.getContentResolver().openInputStream(fileUri);
                sftpChannel.put(sourceStream, destinationPath, monitor, sftpMode);
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
