package eu.renzokuken.sshare.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.util.Log;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSchException;

import java.io.Serializable;

/**
 * Created by renzokuken on 04/12/17.
 */

@Entity
public class HostKeyInfo implements Serializable{

    @Ignore
    private static final String TAG = "HostKeyInfo";

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String hostname;
    @ColumnInfo(name = "key",typeAffinity = ColumnInfo.BLOB)
    public byte[] key;
    public String keyString;
    public String type;

    public HostKeyInfo(){};

    public HostKeyInfo(HostKey hostkey) throws JSchException{
        this.hostname = hostkey.getHost();
        this.keyString = hostkey.getKey();
        this.key = RaceUtils.getRealKey(hostkey.getKey());
        this.type = hostkey.getType();
    }

    public String toString() {
        return this.hostname+" ("+this.type+")";
    }

    public boolean isEqual(HostKey jschKey) {
       if (! this.type.equals(jschKey.getType())) {
            Log.d(TAG, "'" + this.type + "' != '" + jschKey.getType() + "'");
            return false;
        }
        if (! this.keyString.equals(jschKey.getKey())) {
            Log.d(TAG, "'" + this.keyString + "' != '" + jschKey.getKey() + "'");
            return false;
        }
        return true;
    }
}
