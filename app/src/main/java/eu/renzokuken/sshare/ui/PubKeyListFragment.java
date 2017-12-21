package eu.renzokuken.sshare.ui;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ArrayAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by renzokuken on 18/12/17.
 */

public class PubKeyListFragment extends ListFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO  make something better than simple_list_item_1
        PubKeysAdapter pubKeysAdapter = new PubKeysAdapter(this.getActivity(), android.R.layout.simple_list_item_1, getPubKeysList());
        setListAdapter(pubKeysAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        List<File> pubKeysList = getPubKeysList();
        // TODO  make something better than simple_list_item_1
        PubKeyListFragment.PubKeysAdapter pubKeysListAdapter = new PubKeyListFragment.PubKeysAdapter(this.getActivity(), android.R.layout.simple_list_item_1, pubKeysList);
        setListAdapter(pubKeysListAdapter);
    }

    private ArrayList<File> getPubKeysList() {
        return ManagePrivateKeysActivity.getPubKeysList(getActivity());
    }

    private class PubKeysAdapter extends ArrayAdapter<File> {
        public PubKeysAdapter(@NonNull Context context, int resource, @NonNull List<File> objects) {
            super(context, resource, objects);
        }
    }
}