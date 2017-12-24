package eu.renzokuken.sshare;

import android.arch.persistence.room.Room;

import org.junit.Before;
import org.junit.Test;

import eu.renzokuken.sshare.persistence.Connection;
import eu.renzokuken.sshare.persistence.MyDB;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ConnectionUnitTests {
    private Connection testConnection;

    private static String testHostname = "test.host.name";
    private static int testPort = 12345;
    private static String testUsername = "login";

    @Before
    public void init(){
        testConnection = new Connection();
        testConnection.hostname = testHostname;
        testConnection.port = testPort;
        testConnection.protocol = "sftp";
        testConnection.username = testUsername;
    }
    @Test
    public void connectionHostname() throws Exception {
        assertEquals("["+testHostname+":"+testPort+"]", testConnection.getHostString());
    }

    @Test
    public void connectionRemotePath() throws Exception {
        testConnection.remotePath = null;
        assertEquals(null, testConnection.getRemotePath());
        testConnection.remotePath = "";
        assertEquals(null, testConnection.getRemotePath());
        testConnection.remotePath = "lol";
        assertEquals("lol", testConnection.getRemotePath());
        testConnection.remotePath = "./lol";
        assertEquals("./lol", testConnection.getRemotePath());
        testConnection.remotePath = "/lol";
        assertEquals("/lol", testConnection.getRemotePath());
        testConnection.remotePath = "~/lol";
        assertEquals(String.format("/home/%1$s/lol", testUsername), testConnection.getRemotePath());
    }
}