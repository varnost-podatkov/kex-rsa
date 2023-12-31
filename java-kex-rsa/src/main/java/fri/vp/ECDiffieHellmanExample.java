package fri.vp;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.KeyAgreement;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.X509EncodedKeySpec;

public class ECDiffieHellmanExample {
    public static void main(String[] args) {

        final Environment env = new Environment();

        env.add(new Agent("ana") {
            @Override
            public void task() throws Exception {
                final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
                kpg.initialize(256);

                // Generate key pair
                final KeyPair keyPair = kpg.generateKeyPair();

                // send "PK" to bor ("PK": A = g^a, "SK": a)
                send("bor", keyPair.getPublic().getEncoded());
                print("My contribution to ECDH: %s", hex(keyPair.getPublic().getEncoded()));

                // get PK from bor
                final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(receive("bor"));
                final ECPublicKey bobPK = (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(keySpec);

                // Run the agreement protocol
                final KeyAgreement dh = KeyAgreement.getInstance("ECDH");
                dh.init(keyPair.getPrivate());
                dh.doPhase(bobPK, true);

                // generate a shared AES key
                final byte[] sharedSecret = dh.generateSecret();
                print("Shared secret: %s", hex(sharedSecret));
            }
        });

        env.add(new Agent("bor") {
            @Override
            public void task() throws Exception {
                // get PK from ana
                final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(receive("ana"));
                final ECPublicKey alicePK = (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(keySpec);

                final ECParameterSpec dhParamSpec = alicePK.getParams();

                // create your own DH key pair
                final KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
                kpg.initialize(dhParamSpec);
                final KeyPair keyPair = kpg.generateKeyPair();
                send("ana", keyPair.getPublic().getEncoded());
                print("My contribution to ECDH: %s", hex(keyPair.getPublic().getEncoded()));

                final KeyAgreement dh = KeyAgreement.getInstance("ECDH");
                dh.init(keyPair.getPrivate());
                dh.doPhase(alicePK, true);

                final byte[] sharedSecret = dh.generateSecret();
                print("Shared secret: %s", hex(sharedSecret));
            }
        });

        env.connect("ana", "bor");
        env.start();
    }
}
