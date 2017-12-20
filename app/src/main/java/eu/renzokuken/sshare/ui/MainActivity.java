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

import org.w3c.dom.Text;

import java.util.ArrayList;

import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.persistence.MyDB;

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

    public ArrayList<Connection> getConnectionList() {
        MyDB database = MyDB.getDatabase(getApplicationContext());
        return new ArrayList<>(database.connectionDao().getAllConnections());
    }

    @Override
    public void onResume() {
        super.onResume();

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
                Intent pIntent = new Intent(this, ManagePubKeysActivity.class);
                startActivity(pIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
