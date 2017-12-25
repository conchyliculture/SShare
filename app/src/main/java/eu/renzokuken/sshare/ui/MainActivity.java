package eu.renzokuken.sshare.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.persistence.MyDB;
import eu.renzokuken.sshare.persistence.MyDao;
import eu.renzokuken.sshare.upload.FileUploaderService;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.addConnectionFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewAccountActivity.class);
                startActivity(intent);
            }
        });

    }

    public MyDao getDatabaseHandler() {
        MyDB database = MyDB.getDatabase(getApplicationContext());
        return database.connectionDao();
    }

    public ArrayList<Connection> getConnectionList() {

        return new ArrayList<>(getDatabaseHandler().getAllConnections());
    }

    @Override
    public void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            if (action !=null && action.equals(getString(R.string.kill_uploads))){
                Intent newIntent = new Intent(this, FileUploaderService.class);
                newIntent.setAction(getString(R.string.kill_uploads));
                startService(newIntent);
            }
        }
        ArrayList<Connection> connectionList = getConnectionList();

        if (connectionList.isEmpty()) {
            TextView noConnectionTextView = findViewById(R.id.no_connection_textview);
            if (noConnectionTextView != null) {
                noConnectionTextView.setVisibility(View.VISIBLE);
            }

        } else {
            FragmentManager fragMan = getFragmentManager();
            FragmentTransaction fragTransaction = fragMan.beginTransaction();
            ConnectionListFragment connectionListFragment = new ConnectionListFragment();
            fragTransaction.add(R.id.main_linear_layout, connectionListFragment);
            fragTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent sIntent = new Intent(this, SettingsActivity.class);
                startActivity(sIntent);
                return true;
            case R.id.menu_manage_keys:
                Intent pIntent = new Intent(this, ManagePrivateKeysActivity.class);
                startActivity(pIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}