package eu.renzokuken.sshare.upload;

import android.content.Context;
import android.util.Log;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;

import java.util.List;

import eu.renzokuken.sshare.persistence.HostKeyInfo;
import eu.renzokuken.sshare.persistence.MyDB;
import eu.renzokuken.sshare.persistence.MyDao;


/**
 * Created by renzokuken on 08/12/17.
 */


public class MyHostKeyRepository implements HostKeyRepository {
    private static final String TAG = "MyHostKeyRepository" ;
    private MyDao dbConnection;

    public MyHostKeyRepository(Context context) {
        super();
        dbConnection = MyDB.getDatabase(context).connectionDao();
    }

    @Override
    public int check(String host, byte[] key) {
        try {
            HostKey inputHostKey = new HostKey(host, key);
            HostKeyInfo storeHostKey = dbConnection.getHostKeyInfo(host, inputHostKey.getType());
            if (storeHostKey == null) {
                return HostKeyRepository.NOT_INCLUDED;
            }
            if (storeHostKey.isEqual(inputHostKey)) {
                return HostKeyRepository.OK;
            }
            return HostKeyRepository.CHANGED;
        } catch (JSchException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
        return HostKeyRepository.CHANGED;
    }

    @Override
    public void add(HostKey hostkey, UserInfo ui) {
        try {
            ui.promptYesNo("Are you sure you want to add this key? ");
            dbConnection.addHostKey(new HostKeyInfo(hostkey));
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove(String host, String type) {
        HostKeyInfo myHostKey = dbConnection.getHostKeyInfo(host, type);
        dbConnection.deleteHostKey(myHostKey);
    }

    @Override
    public void remove(String host, String type, byte[] key) {
        //TODO check the key?
        remove(host, type);
    }

    @Override
    public String getKnownHostsRepositoryID() {
        return "SShare Repository";
    }

    @Override
    public HostKey[] getHostKey() {
        List<HostKeyInfo> list = dbConnection.getAllHostKeys();
        HostKey[] res = new HostKey[list.size()];
        try {
        for (int i = 0; i < list.size() ; i++) {
            HostKeyInfo elem = list.get(i);
            res[i] = new HostKey(elem.hostname, elem.key);
        }
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return res;
    }

    @Override
    public HostKey[] getHostKey(String host, String type) {
        HostKeyInfo storedKey = dbConnection.getHostKeyInfo(host, type);
        HostKey res = null;
        try {
             res = new HostKey(storedKey.hostname, storedKey.key);
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return new HostKey[]{res};
    }

}

// dummy implementations of the other methods


