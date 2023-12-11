package fri.vp;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

/*
 * Implement a key exchange between Alice and Bob using public-key encryption.
 * Once the shared secret is established, send an encrypted message from Alice to Bob using
 * AES in GCM.
 */
public class A2AgentCommunicationPKEKeyExchange {
    public static void main(String[] args) {
        final Environment env = new Environment();

        env.add(new Agent("ana") {
            @Override
            public void task() throws Exception {
                final KeyPair kp = KeyPairGenerator.getInstance("RSA").generateKeyPair();
                send("bor", kp.getPublic().getEncoded());

                final Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPPadding");
                rsa.init(Cipher.DECRYPT_MODE, kp.getPrivate());
                final byte[] keyBytes = rsa.doFinal(receive("bor"));
                final SecretKeySpec aesKey = new SecretKeySpec(keyBytes, "AES");

                final Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
                aes.init(Cipher.ENCRYPT_MODE, aesKey);
                send("bor", aes.getIV());
                send("bor", aes.doFinal("Hello World!".getBytes(StandardCharsets.UTF_8)));
                print("Done!");
            }
        });

        env.add(new Agent("bor") {
            @Override
            public void task() throws Exception {
                final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(receive("ana"));
                final KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                final PublicKey anaPK = keyFactory.generatePublic(keySpec);

                final SecretKey key = KeyGenerator.getInstance("AES").generateKey();

                final Cipher rsa = Cipher.getInstance("RSA/ECB/OAEPPadding");
                rsa.init(Cipher.ENCRYPT_MODE, anaPK);
                send("ana", rsa.doFinal(key.getEncoded()));

                final Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
                aes.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, receive("ana")));
                print("Got: %s", new String(aes.doFinal(receive("ana")), StandardCharsets.UTF_8));
            }
        });

        env.connect("ana", "bor");
        env.start();
    }
}