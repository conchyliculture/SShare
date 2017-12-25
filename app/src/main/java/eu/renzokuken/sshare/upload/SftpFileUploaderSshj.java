package eu.renzokuken.sshare.upload;

import android.content.Context;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.common.StreamCopier;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.sftp.SFTPException;
import net.schmizz.sshj.xfer.LocalFileFilter;
import net.schmizz.sshj.xfer.LocalSourceFile;
import net.schmizz.sshj.xfer.TransferListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;


class SftpFileUploaderSshj extends FileUploaderSshj {

    SftpFileUploaderSshj(Context context, Connection connection, Monitor monitor) {
        super(context, connection, monitor);
    }

    public void _Push(SSHClient ssh, FileUri fileUri, String destinationPath) throws SShareUploadException {

        MyTransferListener transferListener = new MyTransferListener();
        transferListener.setMonitor(this.monitor);

        try {
            MySFTPClient sftp = new MySFTPClient(ssh.newSFTPClient());
            sftp.setTransferListener(transferListener);
            File destinationFile = new File(fileUri.fileName);
            if (destinationPath != null && !destinationPath.isEmpty()) {
                destinationFile = new File(destinationPath, fileUri.fileName);
            }
            sftp.put(new SSHJLocalSourceFile(fileUri), destinationFile.getPath());
            //sftp.chmod(destinationFile.getPath(), 600); // TODO do a umask thing
            // TODO change modified date if needed
        } catch (ConnectionException e) {
            throw new SShareUploadException(context.getString(R.string.error_connection_closed), e);
        } catch (SFTPException e) {
            switch (e.getStatusCode()) {
                case PERMISSION_DENIED:
                    throw new SShareUploadException(context.getString(R.string.error_sftp_permission_denied));
                case NO_SUCH_FILE:
                    throw new SShareUploadException(
                            context.getString(R.string.error_uploading_file, fileUri.fileName),
                            context.getString(R.string.error_sftp_no_such_file)
                    );
                case UNKNOWN:
                case OK:
                case EOF:
                case FAILURE:
                case BAD_MESSAGE:
                case NO_CONNECTION:
                case CONNECITON_LOST:
                case OP_UNSUPPORTED:
                    throw new SShareUploadException(context.getString(R.string.error_sftp_generic), e);
            }
        } catch (IOException e) {
            if (monitor.shouldStop) {
                throw new SShareUploadException(context.getString(R.string.user_cancelled_upload), e);
            } else {
                throw new SShareUploadException(context.getString(R.string.error_connection_closed), e);
            }
        } catch (SecurityException e) {
            throw new SShareUploadException(context.getString(R.string.error_permission_error), e);
        }
    }

    private class SSHJLocalSourceFile implements LocalSourceFile {
        // TODO extends parent class?
        private final FileUri fileUri;

        public SSHJLocalSourceFile(FileUri fileUri) {
            this.fileUri = fileUri;
        }

        @Override
        public String getName() {
            return fileUri.fileName;
        }

        @Override
        public long getLength() {
            return fileUri.fileSize;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return context.getContentResolver().openInputStream(fileUri.uri);
        }

        @Override
        public int getPermissions() throws IOException {
            return 0;
        }

        @Override
        public boolean isFile() {
            return true;
        }

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public Iterable<? extends LocalSourceFile> getChildren(LocalFileFilter filter) throws IOException {
            return null;
        }

        @Override
        public boolean providesAtimeMtime() {
            return false;
        }

        @Override
        public long getLastAccessTime() throws IOException {
            return 0;
        }

        @Override
        public long getLastModifiedTime() throws IOException {
            return fileUri.dateModified;
        }
    }

    private class MySFTPClient extends SFTPClient {
        public MySFTPClient(SFTPClient sftpClient) {
            super(sftpClient.getSFTPEngine());
        }

        public void setTransferListener(TransferListener transferListener) {
            this.getFileTransfer().setTransferListener(transferListener);
            this.getFileTransfer().setPreserveAttributes(false);
        }
    }

    private class MyTransferListener implements TransferListener {
        private Monitor monitor;

        @Override
        public TransferListener directory(String name) {
            return null;
        }

        public void setMonitor(Monitor monitor) {
            this.monitor = monitor;
        }

        @Override
        public StreamCopier.Listener file(String name, long size) {
            return new StreamCopier.Listener() {

                @Override
                public void reportProgress(long transferred) throws IOException {
                    monitor.notifyProgress(transferred);
                    if (monitor.shouldStop) {
                        // Interrupt transfer
                        throw new IOException();
                    }
                }
            };
        }
    }
}