package eu.renzokuken.sshare.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import eu.renzokuken.sshare.ConnectionHelpers;
import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.persistence.MyDB;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class NewAccountActivity extends AppCompatActivity {

    private static final String TAG = "NewAccountActivity";
    private LinearLayout container;
    private Connection connection;
    private MyDB database;

    private Spinner authSpinner;
    private Spinner protocolSpinner;
    private Button submitButton;
    private TextInputEditText inputHostname;
    private TextInputEditText inputPort;
    private TextInputEditText inputLogin;
    private TextInputEditText inputPassword;
    private TextInputEditText inputRemotePath;

    private boolean isAddingNewConnection = true;
    private String authMode = ConnectionHelpers.DEFAULT_AUTH_MODE;
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
    private ViewGroup credentialsLayout;
    private ViewGroup remotePathLayout;
    private ViewGroup hostnamePortLayout;
    private ViewGroup authModeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = this.getIntent().getBundleExtra("data");

        if (b != null) {
            this.connection = (Connection) b.getSerializable("connection");
            if (this.connection == null) {
                this.connection = new Connection();
            }
            isAddingNewConnection = false;
        }

        setContentView(R.layout.add_activity);
        this.container = findViewById(R.id.addcontainer);
        database = MyDB.getDatabase(getApplicationContext());
        showProtocol();
    }

    private boolean isFormValid() {
        try {

            if (inputHostname.getText().toString().equals(""))
                return false;
            if (inputPort.getText().toString().equals(""))
                return false;
            if (authMode.equals(ConnectionHelpers.AUTH_LP)) {
                if (inputPassword.getText().toString().equals(""))
                    return false;
                if (inputLogin.getText().toString().equals(""))
                    return false;
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

    private void showProtocol() {
        ViewGroup protocolLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.add_protocol, container, true);

        protocolSpinner = findViewById(R.id.spinner_mode);
        final ArrayAdapter<CharSequence> connectionModesAdapter = ArrayAdapter.createFromResource(this,
                R.array.protocol_modes, android.R.layout.simple_spinner_item);

        hostnamePortLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.add_hostname_port, protocolLayout, true);
        inputHostname = hostnamePortLayout.findViewById(R.id.input_hostname);
        inputHostname.addTextChangedListener(globalTextChangedWatcher);
        inputPort = hostnamePortLayout.findViewById(R.id.input_port);
        inputPort.addTextChangedListener(globalTextChangedWatcher);

        if (!isAddingNewConnection) {
            inputHostname.setText(connection.hostname);
            inputPort.setText(String.valueOf(connection.port));
        }

        connectionModesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        protocolSpinner.setAdapter(connectionModesAdapter);
        if (this.connection != null) {
            protocolSpinner.setSelection(ConnectionHelpers.getProtocolPosFromName(this, connection.protocol));
        }

        protocolSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String modeSelected = (String) connectionModesAdapter.getItem(i);
                switch (i) {
                    case 0:
                        showSFTP();
                        break;
                    default:
                        Log.e(TAG, "Unknown protocol " + modeSelected);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //lol
            }
        });
    }

    private void showSFTP() {
        authModeLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.add_auth_mode, hostnamePortLayout, true);

        authSpinner = findViewById(R.id.spinner_auth);

        final ArrayAdapter<CharSequence> authModeAdapter = ArrayAdapter.createFromResource(this,
                R.array.authentication_modes, android.R.layout.simple_spinner_item);

        authModeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        authSpinner.setAdapter(authModeAdapter);
        if (this.connection != null) {
            authSpinner.setSelection(ConnectionHelpers.getAuthenticationPosFromName(this, connection.auth_mode));
        }
        authSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String modeSelected = (String) authModeAdapter.getItem(i);
                switch (i) {
                    case 0:
                        showLoginPassword();
                        break;
                    case 1:
                        showPubKey();
                        break;
                    default:
                        Log.e(TAG, "Unknown auth method " + modeSelected + " " + l);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // LOL
            }
        });
    }

    private void showLoginPassword() {
        authMode = ConnectionHelpers.AUTH_LP;
        credentialsLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.add_login_pwd, authModeLayout, true);
        inputLogin = credentialsLayout.findViewById(R.id.input_login);
        inputLogin.addTextChangedListener(globalTextChangedWatcher);
        inputPassword = credentialsLayout.findViewById(R.id.input_password);
        inputPassword.addTextChangedListener(globalTextChangedWatcher);

        if (this.connection != null) {
            inputLogin.setText(connection.username);
            inputPassword.setText(connection.password);
        }

        showRemotePath();
    }

    private void showPubKey() {
        authMode = ConnectionHelpers.AUTH_KEY;
    }

    private void showRemotePath() {
        remotePathLayout = (ViewGroup) getLayoutInflater().inflate(R.layout.add_remote_path, credentialsLayout, true);
        inputRemotePath = remotePathLayout.findViewById(R.id.input_remote_path);
        inputRemotePath.addTextChangedListener(globalTextChangedWatcher);
        showSubmitButton();
    }

    private void showSubmitButton() {
        getLayoutInflater().inflate(R.layout.add_submit, remotePathLayout, true);
        submitButton = findViewById(R.id.submit_button);
        if (isAddingNewConnection) {
            submitButton.setText(R.string.create_connection);
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAddingNewConnection) {
                    connection = new Connection();
                }
                connection.protocol = ConnectionHelpers.PROTOCOL_IDENTIFIERS.get(protocolSpinner.getSelectedItemPosition());
                connection.hostname = inputHostname.getText().toString();
                connection.port = Integer.parseInt(inputPort.getText().toString());
                connection.auth_mode = ConnectionHelpers.AUTHENTICATION_IDENTIFIERS.get(authSpinner.getSelectedItemPosition());
                switch (connection.auth_mode) {
                    case ConnectionHelpers.AUTH_LP:
                        connection.username = inputLogin.getText().toString();
                        connection.password = inputPassword.getText().toString();
                        break;
                    default:
                        Log.e(TAG, "Unknown auth mode " + connection.auth_mode);
                        break;
                }
                connection.remotePath = inputRemotePath.getText().toString();
                if (isAddingNewConnection) {
                    database.connectionDao().addConnection(connection);
                } else {
                    database.connectionDao().updateConnection(connection);
                }
                Intent intent = new Intent(NewAccountActivity.this, MainActivity.class);
                intent.setFlags(FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}
