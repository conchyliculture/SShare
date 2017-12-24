package eu.renzokuken.sshare;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.persistence.MyDB;
import eu.renzokuken.sshare.persistence.MyDao;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class PersistenceTests {
    private MyDB db;
    private MyDao dao;

    @Before
    public void initDB() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        db = MyDB.getDatabase(appContext);
        dao = db.connectionDao();
    }
    @Test
    public void connectionAdd() throws Exception {
        assertTrue(dao.getAllConnections().isEmpty());
        Connection conn = new Connection();
        conn.username = "login";
        conn.hostname = "host";
        conn.password = "pwd";
        conn.port = 12345;
        dao.addConnection(conn);
        assertEquals(1, dao.getAllConnections().size());
        int connId = dao.getAllConnections().get(0).id;
        conn.port = 12346;
        dao.updateConnection(conn);
        assertEquals(1, dao.getAllConnections().size());
        assertEquals(connId, dao.getAllConnections().get(0).id);
        assertEquals(12346, dao.getAllConnections().get(0).port);
    }
}
