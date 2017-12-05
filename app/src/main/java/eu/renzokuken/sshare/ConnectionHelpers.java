package eu.renzokuken.sshare;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by renzokuken on 05/12/17.
 */

public class ConnectionHelpers {
    final public static String MODE_SFTP = "SFTP";
    final public static String AUTH_LP = "auth_lp";
    final public static String AUTH_KEY = "auth_key";
    public static final String DEFAULT_AUTH_MODE = AUTH_LP;

    // Order is important
    final public static ArrayList<String> PROTOCOL_IDENTIFIERS = new ArrayList<>(Collections.singletonList(MODE_SFTP));
    final public static ArrayList<String> AUTHENTICATION_IDENTIFIERS = new ArrayList<>(Arrays.asList(AUTH_LP, AUTH_KEY));


    public static int getProtocolPosFromName(Context context, String name) {
        String[] prots = context.getResources().getStringArray(R.array.protocol_modes);
        for (int i = 0; i < prots.length; i++) {
            if (prots[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    public static int getAuthenticationPosFromName(Context context, String name) {
        String[] auths = context.getResources().getStringArray(R.array.authentication_modes);
        for (int i = 0; i < auths.length; i++) {
            if (auths[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }
}
