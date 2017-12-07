package eu.renzokuken.sshare.ui;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import eu.renzokuken.sshare.ConnectionHelpers;
import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.upload.FileUploader;
import eu.renzokuken.sshare.upload.SFTPFileUploader;
import eu.renzokuken.sshare.upload.SShareMonitor;

/**
 * Created by renzokuken on 04/12/17.
 */

public class ConnectionListFragment extends ListFragment {

    private static final String TAG = "ConnectionListFragment";
    private Uri fileURI;

    private List<Connection> getConnectionList() {
        MainActivity a = (MainActivity) getActivity();
        return a.getConnectionList();
    }

    @Override
    public void onResume() {
        super.onResume();
        List<Connection> connectionList = getConnectionList();
        ConnectionAdapter connectionListAdapter = new ConnectionAdapter(this.getActivity(), android.R.layout.simple_list_item_1, connectionList);
        setListAdapter(connectionListAdapter);
        ListView listView = getListView();
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            fileURI = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            listView.setOnItemClickListener(new UploadFileOnItemClickListener());
        } else {
            listView.setOnItemClickListener(new EditConnectionOnItemClickListener());
        }
    }

    private class ConnectionAdapter extends ArrayAdapter<Connection> {

        public ConnectionAdapter(@NonNull Context context, int resource, @NonNull List<Connection> objects) {
            super(context, resource, objects);
        }
    }

    private class EditConnectionOnItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Connection connection = (Connection) adapterView.getAdapter().getItem(i);
            Intent intent = new Intent(getActivity(), NewAccountActivity.class);
            Bundle data = new Bundle();
            data.putSerializable("connection", connection);
            intent.putExtra("data", data);
            startActivity(intent);
        }
    }

    private class UploadFileOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            FileUploader fileUploader = null;
            Connection connection = ((MainActivity) getActivity()).getConnectionList().get(i);
            Log.i(TAG, "Uploading " + fileURI.toString() + " to " + connection.toString());
            if (connection.protocol.equals(ConnectionHelpers.MODE_SFTP)) {
                fileUploader = new SFTPFileUploader(getActivity());
            } else {
                Log.e(TAG, "Protocol " + connection.protocol + " not implemented =(");
            }

            SShareMonitor monitor = new SShareMonitor(getActivity());

            if (fileUploader != null) {
                fileUploader.uploadFile(connection, fileURI, monitor);
            }
        }
    }
}