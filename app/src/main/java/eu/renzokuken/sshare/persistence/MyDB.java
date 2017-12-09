package eu.renzokuken.sshare.persistence;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

/**
 * Created by renzokuken on 04/12/17.
 */
@Database(entities = {Connection.class, HostKeyInfo.class}, version = 1, exportSchema = false)
public abstract class MyDB extends RoomDatabase {
    private static MyDB INSTANCE;

    public abstract MyDao connectionDao();

    public static MyDB getDatabase(Context context) {
        if (INSTANCE == null) {
            //context.deleteDatabase("database");

            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    MyDB.class, "database").allowMainThreadQueries().build();
        }
        return INSTANCE;
    }
}