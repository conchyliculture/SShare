package eu.renzokuken.sshare.ui;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.security.Security;
import java.util.ArrayList;
import java.util.List;

import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.upload.FileUploaderService;

/**
 * Created by renzokuken on 04/12/17.
 */

public class ConnectionListFragment extends ListFragment {

    private static final String TAG = "ConnectionListFragment";

    private List<Connection> getConnectionList() {
        MainActivity a = (MainActivity) getActivity();
        return a.getConnectionList();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Need to remove outdated BouncyCastle Android crap, and insert SpongyCastle
        Security.removeProvider("BC");
        //Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
        Security.addProvider(new org.spongycastle.jce.provider.BouncyCastleProvider());
        Log.d(TAG, "did the magic security dance");
        List<Connection> connectionList = getConnectionList();
        // TODO  make something better than simple_list_item_1
        ConnectionAdapter connectionListAdapter = new ConnectionAdapter(this.getActivity(), android.R.layout.simple_list_item_1, connectionList);
        setListAdapter(connectionListAdapter);
        ListView listView = getListView();
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            listView.setOnItemClickListener(new UploadFileServiceOnItemClickListener(intent));
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            listView.setOnItemClickListener(new UploadFileServiceOnItemClickListener(intent));
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

    private class UploadFileServiceOnItemClickListener implements AdapterView.OnItemClickListener {
        private final ArrayList<Uri> fileURIs = new ArrayList<>();

        public UploadFileServiceOnItemClickListener(Intent intent) {
            if (Intent.ACTION_SEND.equals(intent.getAction())) {
                fileURIs.add((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                for (Parcelable p: intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)) {
                    fileURIs.add((Uri) p);
                }
            }
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Connection connection = ((MainActivity) getActivity()).getConnectionList().get(i);
            for (Uri fileURI:fileURIs) {
                Log.i(TAG, "Uploading " + fileURI + " to " + connection.toString());
                Intent intent = new Intent(getActivity(), FileUploaderService.class);
                intent.putExtra("fileURI", fileURI);
                intent.putExtra("connection", connection);
                getActivity().startService(intent);

            }
        }
    }
}