package eu.renzokuken.sshare.ui;

import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import eu.renzokuken.sshare.R;

/**
 * Created by renzokuken on 18/12/17.
 */

public class PubKeyListFragment extends ListFragment {

    private PubKeysAdapter pubKeysListAdapter;

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

        final List<File> pubKeysList = getPubKeysList();
        // TODO  make something better than simple_list_item_1
        pubKeysListAdapter = new PubKeyListFragment.PubKeysAdapter(this.getActivity(), android.R.layout.simple_list_item_1, pubKeysList);
        setListAdapter(pubKeysListAdapter);

        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                final File keyFile = pubKeysList.get(position);
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(getActivity());
                builderSingle.setIcon(R.drawable.ic_info_black_24dp);
                builderSingle.setTitle(getString(R.string.attention_title));
                builderSingle.setMessage(getString(R.string.key_file_delete_confirmation_message, keyFile.getName()));
                builderSingle.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        removeKeyFile(keyFile);
                        pubKeysListAdapter.notifyDataSetChanged();
                    }
                });
                builderSingle.setCancelable(true);
                builderSingle.setNegativeButton(android.R.string.no, null);
                builderSingle.show();

                return true;
            }
        });
    }

    private void removeKeyFile(File keyFile) {

        File dir = getActivity().getFilesDir();
        File file = new File(dir, keyFile.getName());
        boolean deleted = file.delete();
        if (!deleted) {
            Toast.makeText(getActivity(), getString(R.string.error_could_not_delete_key_file, keyFile.getName()), Toast.LENGTH_LONG).show();
        } else {
            pubKeysListAdapter.remove(keyFile);
        }
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