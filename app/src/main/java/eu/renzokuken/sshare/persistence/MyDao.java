package eu.renzokuken.sshare.persistence;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by renzokuken on 04/12/17.
 */

@Dao
public interface MyDao {
    @Insert
    void addConnection(Connection... connections);

    @Update
    void updateConnection(Connection... connections);

    @Delete
    void deleteConnection(Connection... connections);

    @Query("SELECT * FROM connection")
    List<Connection> getAllConnections();

    @Query("SELECT * FROM connection WHERE id = :connectionId")
    Connection getConnectionById(int connectionId);

    @Insert
    void addHostKey(HostKeyInfo... hostKey);

    @Update
    void updateHostKey(HostKeyInfo... hostKey);

    @Delete
    void deleteHostKey(HostKeyInfo... hostKey);

    @Query("SELECT * FROM HostKeyInfo WHERE hostname = :hostname and type = :type")
    HostKeyInfo getHostKeyInfo(String hostname, String type);

    @Query("SELECT * FROM HostKeyInfo")
    List<HostKeyInfo> getAllHostKeys();

}
