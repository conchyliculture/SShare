package eu.renzokuken.sshare.persistence;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by renzokuken on 04/12/17.
 */

@Entity
public class Connection implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String hostname;
    public int port;
    public String remotePath;
    public String key;
    public String username;
    public String password;
    public String protocol;
    public String auth_mode;

    public String toString() {
        if (this.remotePath==null || this.remotePath.isEmpty()) {
            return this.protocol + "://" + this.username + "@" + this.hostname + ":" + this.port;
        } else {
            return this.protocol + "://" + this.username + "@" + this.hostname + ":" + this.port + "/" + remotePath;

        }
    }

    public String getHostString() {
        return "[" + hostname + ":" + this.port + "]";
    }
}
