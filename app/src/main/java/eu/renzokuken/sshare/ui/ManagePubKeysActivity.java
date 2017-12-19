package eu.renzokuken.sshare.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import net.schmizz.sshj.userauth.keyprovider.KeyFormat;
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import eu.renzokuken.sshare.R;
import eu.renzokuken.sshare.upload.FileUri;

public class ManagePubKeysActivity extends AppCompatActivity {

    private static final String TAG = "ManagePubKeysActivity";
    private static final int FILE_SELECT_CODE = 1;

    private static boolean isValidKeyFile(File file) {
        try {
            KeyFormat format = KeyProviderUtil.detectKeyFileFormat(file);
            return format != KeyFormat.Unknown;
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "Error reading" + file.getPath());
        }
        return false;
    }

    static ArrayList<File> getPubKeysList(Context context) {
        ArrayList<File> pubKeyFileList = new ArrayList<>();
        File keyDir = context.getFilesDir();
        for (File keyFile : keyDir.listFiles()) {
            if (isValidKeyFile(keyFile)) {
                pubKeyFileList.add(keyFile);
            } else {
                Log.d(TAG, "file " + keyFile.getName() + " is not a valid key file");
            }
        }
        Log.d(TAG, "len " + pubKeyFileList.size());
        return pubKeyFileList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_pub_keys);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<File> pubKeysList = getPubKeysList(this);

        if (pubKeysList.isEmpty()) {
            TextView noPubKeyTextView = findViewById(R.id.no_pubkey_textview);
            if (noPubKeyTextView != null) {
                noPubKeyTextView.setVisibility(View.VISIBLE);
            }

        } else {
            FragmentManager fragMan = getFragmentManager();
            FragmentTransaction fragTransaction = fragMan.beginTransaction();
            PubKeyListFragment pubKeyListFragment = new PubKeyListFragment();
            fragTransaction.add(R.id.manager_pubkey_activity_container, pubKeyListFragment);
            fragTransaction.commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pubkeys, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //TODO: generate keypair
//            case R.id.menu_generate_key:
//                DialogFragment newFragment = new GenerateKeyDialog();
//                newFragment.show(getFragmentManager(), "GenerateKey");
//                return true;
            case R.id.menu_import_key:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "Select file"), FILE_SELECT_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE) {
            if (resultCode == RESULT_OK) {
                Uri keyFileUri = data.getData();
                File file = copyToStorage(keyFileUri);
                boolean result = isValidKeyFile(file);
                if (result) {
                    Toast.makeText(this, "Unable to load key " + keyFileUri.getPath(), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private File copyToStorage(Uri uri) {
        FileUri inputFileUri = new FileUri(this, uri);
        String fileName = inputFileUri.fileName;
        try {
            OutputStream outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            InputStream inputStream = getContentResolver().openInputStream(uri);
            try {
                byte[] buffer = new byte[4 * 1024]; // or other buffer size
                int read;

                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }

                outputStream.flush();
            } finally {
                outputStream.close();
                inputStream.close();
            }

            return new File(getFilesDir(), fileName);

        } catch (FileNotFoundException e) {
            Log.d(TAG, "Can't find" + fileName);
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Can't open" + fileName);
            e.printStackTrace();
        }
        return null;
    }
}