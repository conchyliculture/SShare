package eu.renzokuken.sshare.upload;

import com.jcraft.jsch.HostKeyRepository;

/**
 * Created by renzokuken on 08/12/17.
 */

public class HostKeyFingerprintRepository extends HostKeyRepository
    {
        int check(String host, byte[] key)
        {
            // Based on KeyExchange.getFingerPrint
            Class c = Class.forName(jsch.getConfig("md5"));
            HASH hash = (HASH)(c.newInstance());
            // Based on Util.getFingerPrint
            hash.init();
            hash.update(key, 0, key.length);
            byte[] foo=hash.digest();
            StringBuffer sb=new StringBuffer();
            int bar;
            for(int i=0; i<foo.length;i++){
                bar=foo[i]&0xff;
                sb.append(chars[(bar>>>4)&0xf]);
                sb.append(chars[(bar)&0xf]);
                if(i+1<foo.length)
                    sb.append(":");
            }
            String fingerprint = sb.toString();
            if (fingerprint.equals("2b:16:18:83:7b:c6:5e:49:f2:f0:8e:e2:dc:64:da:1a"))
            {
                return OK;
            }
            else
            {
                return NOT_INCLUDED;
            }
        }

        // dummy implementations of the other methods
    }

