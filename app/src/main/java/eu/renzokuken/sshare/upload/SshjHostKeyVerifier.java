package eu.renzokuken.sshare.upload;

import android.content.Context;
import android.util.Log;

import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

import java.security.PublicKey;

import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.persistence.HostKeyInfo;
import eu.renzokuken.sshare.persistence.MyDB;
import eu.renzokuken.sshare.persistence.MyDao;

/**
 * Created by renzokuken on 09/12/17.
 */

class SshjHostKeyVerifier implements HostKeyVerifier {
    private static final String TAG = "SshjHostKeyVerifier" ;
    private final MyDao dbConnection;
    private PublicKey lastSeenKey;

    public SshjHostKeyVerifier(Context context) {
        super();
        dbConnection = MyDB.getDatabase(context).connectionDao();
    }

    @Override
    public boolean verify(String hostname, int port, PublicKey key) {
        lastSeenKey = key;
        String fingerPrint = SecurityUtils.getFingerprint(key);
        HostKeyInfo storedHostKey = dbConnection.getHostKeyInfo("["+hostname+":"+port+"]", key.getAlgorithm());
        if (storedHostKey == null) {
            Log.d(TAG, "On a pas la clé =(");
            return false;
        }
        return fingerPrint.equals(storedHostKey.keyString);
    }

    public PublicKey getLastSeenKey() {
        return lastSeenKey;
    }

    public void addKey(Connection connection, PublicKey key) {
        HostKeyInfo keyInfo = new HostKeyInfo();
        keyInfo.hostname = connection.getHostString();
        keyInfo.keyString = SecurityUtils.getFingerprint(key);
        keyInfo.type = key.getAlgorithm();
        dbConnection.addHostKey(keyInfo);
        Log.d(TAG, "Added new key"+keyInfo);
    }
}
