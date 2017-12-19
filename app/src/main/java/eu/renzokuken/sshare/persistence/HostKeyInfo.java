package eu.renzokuken.sshare.persistence;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by renzokuken on 04/12/17.
 */

@Entity
public class HostKeyInfo implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;
    public String hostname;
    @ColumnInfo(name = "key", typeAffinity = ColumnInfo.BLOB)
    public byte[] key;
    public String keyString;
    public String type;

    public HostKeyInfo() {
    }

    public String toString() {
        return this.hostname + " (" + this.type + ")";
    }
}
