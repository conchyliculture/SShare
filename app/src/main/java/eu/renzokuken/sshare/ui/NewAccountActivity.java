package eu.renzokuken.sshare.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.ViewSwitcher;

import java.io.File;
import java.util.ArrayList;

import eu.renzokuken.sshare.ConnectionConstants;
import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.persistence.MyDB;

import static eu.renzokuken.sshare.ConnectionConstants.AuthenticationMethod.ENUM_AUTH_KEY;
import static eu.renzokuken.sshare.ConnectionConstants.AuthenticationMethod.ENUM_AUTH_LP;
import static eu.renzokuken.sshare.ConnectionConstants.ProtocolMethod.ENUM_PROTO_SFTP;

public class NewAccountActivity extends AppCompatActivity {

    private Connection connection;
    private MyDB database;

    private Button selectKeyButton;
    private Button submitButton;
    private TextInputEditText inputHostname;
    private TextInputEditText inputPort;
    private TextInputEditText inputLogin;
    private TextInputEditText inputPassword;
    private TextInputEditText inputRemotePath;

    private boolean isAddingNewConnection = true;
    private ConnectionConstants.AuthenticationMethod authModeSelected = ENUM_AUTH_LP;
    private final TextWatcher globalTextChangedWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            validateForm();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };
    private ConnectionConstants.ProtocolMethod protocolSelected = ENUM_PROTO_SFTP;
    private File privateKeyFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = this.getIntent().getBundleExtra("data");

        if (b != null) {
            this.connection = (Connection) b.getSerializable("connection");
            if (this.connection != null) {
                isAddingNewConnection = false;
            }
        }

        if (this.connection == null) {
            this.connection = new Connection();
            this.connection.auth_mode = ConnectionConstants.DEFAULT_AUTH_MODE;
        }

        setContentView(R.layout.activity_add_connection);
        database = MyDB.getDatabase(getApplicationContext());

        // Set protocol chooser.
        Spinner protocolSpinner = findViewById(R.id.spinner_protocols);
        final ArrayAdapter<String> connectionProtocolsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (ConnectionConstants.ProtocolMethod a : ConnectionConstants.ProtocolMethod.values()) {
            connectionProtocolsAdapter.add(a.getText(this));
        }

        connectionProtocolsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocolSpinner.setAdapter(connectionProtocolsAdapter);
        protocolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                protocolSelected = ConnectionConstants.ProtocolMethod.findById(i);
                connection.protocol = protocolSelected.getDbKey();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Set authentication mode chooser.
        final ViewSwitcher authModeSwitcher = findViewById(R.id.auth_mode_switcher);
        Spinner authSpinner = findViewById(R.id.spinner_auth);
        final ArrayAdapter<String> authModeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        for (ConnectionConstants.AuthenticationMethod a : ConnectionConstants.AuthenticationMethod.values()) {
            authModeAdapter.add(a.getText(this));
        }
        authModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        authSpinner.setAdapter(authModeAdapter);
        authSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                authModeSwitcher.setDisplayedChild(i);
                authModeSelected = ConnectionConstants.AuthenticationMethod.findById(i);
                connection.auth_mode = authModeSelected.getDbKey();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // Set hostname & port chooser.
        inputHostname = findViewById(R.id.input_hostname);
        inputHostname.addTextChangedListener(globalTextChangedWatcher);
        inputPort = findViewById(R.id.input_port);
        inputPort.addTextChangedListener(globalTextChangedWatcher);

        // Set login & password view
        inputLogin = findViewById(R.id.input_login);
        inputLogin.addTextChangedListener(globalTextChangedWatcher);
        inputPassword = findViewById(R.id.input_password);
        inputPassword.addTextChangedListener(globalTextChangedWatcher);

        selectKeyButton = findViewById(R.id.select_key_button);
        selectKeyButton.addTextChangedListener(globalTextChangedWatcher);
        selectKeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builderSingle = new AlertDialog.Builder(NewAccountActivity.this);
                // TODO: get a nice icon
                // builderSingle.setIcon(R.drawable.ic_launcher);
                builderSingle.setTitle("Select Key");
                final ArrayList<File> keyFilesList = ManagePubKeysActivity.getPubKeysList(NewAccountActivity.this);
                final ArrayAdapter<String> keyFilesAdapter = new ArrayAdapter<>(NewAccountActivity.this, android.R.layout.select_dialog_singlechoice);
                for (File key : keyFilesList) {
                    keyFilesAdapter.add(key.getName());
                }
                builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builderSingle.setAdapter(keyFilesAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        privateKeyFile = keyFilesList.get(which);
                        selectKeyButton.setText(privateKeyFile.getName());
                    }
                });
                builderSingle.show();
            }
        });

        inputRemotePath = findViewById(R.id.input_remote_path);
        inputRemotePath.addTextChangedListener(globalTextChangedWatcher);

        submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connection.hostname = inputHostname.getText().toString();
                connection.port = Integer.parseInt(inputPort.getText().toString());
                connection.username = inputLogin.getText().toString();
                switch (authModeSelected) {
                    case ENUM_AUTH_LP:
                        connection.password = inputPassword.getText().toString();
                        break;
                    case ENUM_AUTH_KEY:
                        connection.key = privateKeyFile.getPath();
                        break;
                }
                connection.remotePath = inputRemotePath.getText().toString();
                if (isAddingNewConnection) {
                    database.connectionDao().addConnection(connection);
                } else {
                    database.connectionDao().updateConnection(connection);
                }
                finish();
            }
        });

        if (!isAddingNewConnection) {
            protocolSpinner.setSelection(ConnectionConstants.ProtocolMethod.findByDbKey(connection.protocol).getId());
            authSpinner.setSelection(ConnectionConstants.AuthenticationMethod.findByDbKey(connection.auth_mode).getId());
            inputHostname.setText(connection.hostname);
            inputPort.setText(String.valueOf(connection.port));
            switch (ConnectionConstants.AuthenticationMethod.findByDbKey(connection.auth_mode)) {
                case ENUM_AUTH_LP:
                    inputLogin.setText(connection.username);
                    inputPassword.setText(connection.password);
                    break;
                case ENUM_AUTH_KEY:
                    privateKeyFile = new File(connection.key);
                    String buttonMsg = "Change key (current: " + privateKeyFile.getName() + ")";
                    selectKeyButton.setText(buttonMsg);
                    break;
            }
            inputRemotePath.setText(connection.remotePath);
            submitButton.setText(R.string.update_connection);
        }
    }

    private boolean isFormValid() {
        try {

            if (inputHostname.getText().toString().equals(""))
                return false;
            if (inputPort.getText().toString().equals(""))
                return false;
            if (authModeSelected.equals(ENUM_AUTH_LP)) {
                if (inputPassword.getText().toString().equals(""))
                    return false;
                if (inputLogin.getText().toString().equals(""))
                    return false;
            } else if (authModeSelected.equals(ENUM_AUTH_KEY)) {
                if (selectKeyButton.getText().toString().equals(getString(R.string.select_key_message_button))) {
                    return false;
                }
            }
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    private void validateForm() {
        if (submitButton != null)
            submitButton.setEnabled(isFormValid());
    }
}