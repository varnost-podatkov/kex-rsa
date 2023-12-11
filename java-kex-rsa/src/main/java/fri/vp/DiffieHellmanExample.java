package fri.vp;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.X509EncodedKeySpec;

public class DiffieHellmanExample {
    public static void main(String[] args) {

        final Environment env = new Environment();

        env.add(new Agent("alice") {
            @Override
            public void task() throws Exception {
                final KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
                kpg.initialize(2048);

                // Generate key pair
                final KeyPair keyPair = kpg.generateKeyPair();

                // send "PK" to bob ("PK": A = g^a, "SK": a)
                send("bob", keyPair.getPublic().getEncoded());
                print("My contribution: A = g^a = %s",
                        hex(keyPair.getPublic().getEncoded()));

                // get PK from bob
                final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(receive("bob"));
                final DHPublicKey bobPK = (DHPublicKey) KeyFactory.getInstance("DH")
                        .generatePublic(keySpec);

                // Run the agreement protocol
                final KeyAgreement dh = KeyAgreement.getInstance("DH");
                dh.init(keyPair.getPrivate());
                dh.doPhase(bobPK, true);

                // generate a shared AES key
                final byte[] sharedSecret = dh.generateSecret();
                print("Shared secret: g^ab = B^a = %s", hex(sharedSecret));
            }
        });

        env.add(new Agent("bob") {
            @Override
            public void task() throws Exception {
                // get PK from alice
                final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(
                        receive("alice"));
                final DHPublicKey alicePK = (DHPublicKey) KeyFactory.getInstance("DH")
                        .generatePublic(keySpec);

                final DHParameterSpec dhParamSpec = alicePK.getParams();

                // create your own DH key pair
                final KeyPairGenerator kpg = KeyPairGenerator.getInstance("DH");
                kpg.initialize(dhParamSpec);
                final KeyPair keyPair = kpg.generateKeyPair();
                send("alice", keyPair.getPublic().getEncoded());
                print("My contribution: B = g^b = %s",
                        hex(keyPair.getPublic().getEncoded()));

                final KeyAgreement dh = KeyAgreement.getInstance("DH");
                dh.init(keyPair.getPrivate());
                dh.doPhase(alicePK, true);

                final byte[] sharedSecret = dh.generateSecret();
                print("Shared secret: g^ab = A^b = %s", hex(sharedSecret));
            }
        });

        env.connect("alice", "bob");
        env.start();
    }
}
