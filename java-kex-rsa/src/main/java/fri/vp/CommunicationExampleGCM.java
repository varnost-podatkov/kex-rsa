package fri.vp;

import fri.isp.Agent;
import fri.isp.Environment;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;


/**
 * To je primer simulacijskega okolja, v katerem si agenti (Ana, Bor, Cene, Nandi idr.)
 * izmenjujejo sporočila.
 * <p>
 * Vsak agent se izvaja v svoji niti in neodvisno od ostalih. S pomočjo klicev funkcije
 * send(String) in receive(String) agenti pošiljajo in prejemajo sporočila.
 * <p>
 * Enota poslanega podatka je polje bajtov, zato je treba vsak podatek pred
 * pošiljanjem postrojiti.
 */
public class CommunicationExampleGCM {
    public static void main(String[] args) throws Exception {
        // Simulacijsko okolje, v katerem bivajo agenti
        final Environment env = new Environment();

        // Mesto za deklaracijo globalnih spremenljivk
        final Key key = KeyGenerator.getInstance("AES").generateKey();

        env.add(new Agent("ana") {
            @Override
            public void task() throws Exception {
                // Uporabite AES-GCM in zavarujte komunikacijsko sejo, tako da bodo vsa sporočila šifrirana
                final byte[] pt = "Zdravo Bor, tukaj Ana.".getBytes(StandardCharsets.UTF_8);
                final Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
                aes.init(Cipher.ENCRYPT_MODE, key);
                send("bor", aes.doFinal(pt));
                send("bor", aes.getIV());
                print("Poslala sem sporočilo.");
            }
        });

        env.add(new Agent("bor") {
            @Override
            public void task() throws Exception {
                // Bor prejme sporočilo
                final byte[] ct = receive("ana");
                final byte[] iv = receive("ana");

                final Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
                aes.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
                final byte[] pt = aes.doFinal(ct);

                // Bor sporočilo izpiše
                print("Sporočilo se glasi: '%s'", new String(pt, StandardCharsets.UTF_8));

            }
        });

        // Povežemo Ano in Bora
        env.connect("ana", "bor");
        // zaženemo simulacijsko okolje
        env.start();
    }
}
