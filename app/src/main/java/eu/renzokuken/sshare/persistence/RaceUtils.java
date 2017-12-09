package eu.renzokuken.sshare.persistence;

/**
 * Created by renzokuken on 09/12/17.
 */

public class RaceUtils {
    /* SMELIFJH QSKLEDFH QNMKSDHN QLKR;

     */
    static byte[] str2byte(String str, String encoding){
        if(str==null)
            return null;
        try{ return str.getBytes(encoding); }
        catch(java.io.UnsupportedEncodingException e){
            return str.getBytes();
        }
    }
    static byte[] str2byte(String str){
        return str2byte(str, "UTF-8");
    }
    private static final byte[] b64 =str2byte("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=");
    private static byte val(byte foo){
        if(foo == '=') return 0;
        for(int j=0; j<b64.length; j++){
            if(foo==b64[j]) return (byte)j;
        }
        return 0;
    }
    static byte[] fromBase64(byte[] buf, int start, int length){
        byte[] foo=new byte[length];
        int j=0;
        for (int i=start;i<start+length;i+=4){
            foo[j]=(byte)((val(buf[i])<<2)|((val(buf[i+1])&0x30)>>>4));
            if(buf[i+2]==(byte)'='){ j++; break;}
            foo[j+1]=(byte)(((val(buf[i+1])&0x0f)<<4)|((val(buf[i+2])&0x3c)>>>2));
            if(buf[i+3]==(byte)'='){ j+=2; break;}
            foo[j+2]=(byte)(((val(buf[i+2])&0x03)<<6)|(val(buf[i+3])&0x3f));
            j+=3;
        }
        byte[] bar=new byte[j];
        System.arraycopy(foo, 0, bar, 0, j);
        return bar;
    }

    public static byte[] getRealKey(String stringKey){
        return fromBase64(str2byte(stringKey),0 , stringKey.length());
    }
}
