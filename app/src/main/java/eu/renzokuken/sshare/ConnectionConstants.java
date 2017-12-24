package eu.renzokuken.sshare;

import android.content.Context;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by renzokuken on 05/12/17.
 */

public class ConnectionConstants {
    final private static String PROTO_SFTP = "sftp";
    final private static String AUTH_LP = "auth_lp";
    public static final String DEFAULT_AUTH_MODE = AUTH_LP;
    final private static String AUTH_KEY = "auth_key";

    @SuppressWarnings("SameParameterValue")  //Remove when we support something else than SFTP
    public enum ProtocolMethod implements EnumWithId {
        ENUM_PROTO_SFTP(0, PROTO_SFTP, R.string.sftp_protocol),;

        private static final EnumIdLookup<ProtocolMethod> lookupID = new EnumIdLookup<>(ProtocolMethod.class);
        private static final EnumDbLookup<ProtocolMethod> lookupDB = new EnumDbLookup<>(ProtocolMethod.class);
        private final int id;
        private final int textId;
        private final String dbKey;

        ProtocolMethod(int id, String dbKey, int textId) {
            this.id = id;
            this.textId = textId;
            this.dbKey = dbKey;
        }

        public static ProtocolMethod findById(int id) {
            return lookupID.get(id);
        }

        public static ProtocolMethod findByDbKey(String dbKey) {
            return lookupDB.get(dbKey);
        }

        @Override
        public int getId() {
            return this.id;
        }

        public String getText(Context context) {
            return context.getString(this.textId);
        }

        public String getDbKey() {
            return dbKey;
        }
    }

    public enum AuthenticationMethod implements EnumWithId {
        ENUM_AUTH_LP(0, AUTH_LP, R.string.password_authentication),
        ENUM_AUTH_KEY(1, AUTH_KEY, R.string.key_authentication);

        private static final EnumIdLookup<AuthenticationMethod> lookupID = new EnumIdLookup<>(AuthenticationMethod.class);
        private static final EnumDbLookup<AuthenticationMethod> lookupDB = new EnumDbLookup<>(AuthenticationMethod.class);
        private final int id;
        private final int textId;
        private final String dbKey;

        AuthenticationMethod(int id, String dbKey, int textId) {
            this.id = id;
            this.textId = textId;
            this.dbKey = dbKey;
        }

        public static AuthenticationMethod findById(int id) {
            return lookupID.get(id);
        }

        public static AuthenticationMethod findByDbKey(String dbKey) {
            return lookupDB.get(dbKey);
        }

        @Override
        public int getId() {
            return this.id;
        }

        public String getText(Context context) {
            return context.getString(this.textId);
        }

        public String getDbKey() {
            return this.dbKey;
        }
    }

    public static class EnumIdLookup<E extends Enum<E> & EnumWithId> {

        private final SparseArray<E> map = new SparseArray<>();

        public EnumIdLookup(Class<E> enumType) {
            for (E v : enumType.getEnumConstants()) {
                map.put(v.getId(), v);
            }
        }

        public E get(int num) {
            return map.get(num);
        }
    }

    public static class EnumDbLookup<E extends Enum<E> & EnumWithId> {

        private final HashMap<String, E> map = new HashMap<>();

        public EnumDbLookup(Class<E> enumType) {
            for (E v : enumType.getEnumConstants()) {
                map.put(v.getDbKey(), v);
            }
        }

        public E get(String dbKey) {
            return map.get(dbKey);
        }
    }
}
