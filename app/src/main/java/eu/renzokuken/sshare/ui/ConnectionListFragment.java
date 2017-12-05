package eu.renzokuken.sshare.ui;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.List;

import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.persistence.MyDB;

/**
 * Created by renzokuken on 04/12/17.
 */

public class ConnectionListFragment extends ListFragment {

    private static final String TAG = "ConnectionListFragment";

    private class ConnectionAdapter extends ArrayAdapter<Connection> {

        public ConnectionAdapter(@NonNull Context context, int resource, @NonNull List<Connection> objects) {
            super(context, resource, objects);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyDB database = MyDB.getDatabase(getActivity().getApplicationContext());
        List<Connection> list = database.connectionDao().getAllConnections();
        ConnectionAdapter connectionListAdapter = new ConnectionAdapter(this.getActivity(), android.R.layout.simple_list_item_1 , list);
        setListAdapter(connectionListAdapter);

    }
    @Override
    public void onResume() {
        super.onResume();
        ListView listView = getListView();
        if (getActivity() instanceof MainActivity) {
            listView.setOnItemClickListener(new EditConnectionOnItemClickListener());
        } else if (getActivity() instanceof ShareActivity) {
            listView.setOnItemClickListener(new UploadFileOnItemClickListener());
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
            Log.i(TAG, "TODO: upload");
        }
    }
}