package eu.renzokuken.sshare.ui;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.upload.FileUploaderService;

/**
 * Created by renzokuken on 04/12/17.
 */

public class ConnectionListFragment extends ListFragment {

    private MainActivity mainActivity ;
    private ArrayList<Connection> connectionList;

    private void removeConnection(Connection connection){
        mainActivity.getDatabaseHandler().deleteConnection(connection);
        connectionList.remove(connection);
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity = (MainActivity) getActivity();
        connectionList = mainActivity.getConnectionList();
        // TODO  make something better than simple_list_item_1
        final ConnectionAdapter connectionListAdapter = new ConnectionAdapter(this.getActivity(), android.R.layout.simple_list_item_1, connectionList);
        setListAdapter(connectionListAdapter);
        ListView listView = getListView();
        Intent intent = getActivity().getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            TextView connectionListTextView = mainActivity.findViewById(R.id.connection_list_text_view);
            connectionListTextView.setText(getString(R.string.select_connection_title));
            listView.setOnItemClickListener(new UploadFileOnItemClickListener(intent));
        } else {
            TextView connectionListTextView = mainActivity.findViewById(R.id.connection_list_text_view);
            connectionListTextView.setText(getString(R.string.connection_list_title));
            listView.setOnItemClickListener(new EditConnectionOnItemClickListener());
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                    final Connection connection = connectionList.get(position);
                    AlertDialog.Builder builderSingle = new AlertDialog.Builder(mainActivity);
                    builderSingle.setIcon(R.drawable.ic_info_black_24dp);
                    builderSingle.setTitle(getString(R.string.attention_title));
                    builderSingle.setMessage(getString(R.string.connection_delete_confirmation_message, connection.getHostString()));
                    builderSingle.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            removeConnection(connection);
                            connectionListAdapter.notifyDataSetChanged();
                        }
                    });
                    builderSingle.setCancelable(true);
                    builderSingle.setNegativeButton(android.R.string.no, null);
                    builderSingle.show();

                    return true;
                }
            });
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
            data.putSerializable(getString(R.string.connection_handle), connection);
            intent.putExtra(getString(R.string.data_handle), data);
            startActivity(intent);
        }
    }

    private class UploadFileOnItemClickListener implements AdapterView.OnItemClickListener {
        private final ArrayList<Uri> fileURIs = new ArrayList<>();

        public UploadFileOnItemClickListener(Intent intent) {
            if (Intent.ACTION_SEND.equals(intent.getAction())) {
                fileURIs.add((Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
                for (Parcelable p : intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)) {
                    fileURIs.add((Uri) p);
                }
            }
        }

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Connection connection = ((MainActivity) getActivity()).getConnectionList().get(i);
            for (Uri fileURI : fileURIs) {
                Intent intent = new Intent(getActivity(), FileUploaderService.class);
                intent.setAction(getString(R.string.new_upload_action));
                intent.setData(fileURI);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(getString(R.string.connection_handle), connection);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //Pass permission from content provider
                getActivity().startService(intent);
                getActivity().finish();
            }
        }
    }
}