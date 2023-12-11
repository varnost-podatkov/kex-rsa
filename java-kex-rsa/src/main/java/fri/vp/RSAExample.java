package fri.vp;

import fri.isp.Agent;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 * - Poskusite ročno nastaviti velikost modula (ključa)
 * - Poskusite podlogo nastaviti na "NoPadding" in šifrirajte in dešifrirajte. Zakaj je dolžina dešifriranega
 * tajnopisa daljša od čistopisa? Kako bi to popravili?
 */
public class RSAExample {
    public static void main(String[] args) throws Exception {
        // Pojasnilo parametrov
        //  - Način je ECB: vsak blok se šifrira zase (to je pri RSA sprejemljivo, pri bločnih šifrah pa ne)
        //  - Nastavimo podlogo na OAEP (poglejte v dokumentacijo, kaj še obstaja)
        final String algorithm = "RSA/ECB/OAEPPadding";
        final String message = "A test message.";
        final byte[] pt = message.getBytes(StandardCharsets.UTF_8);

        System.out.println("Message: " + message);
        System.out.println("PT: " + Agent.hex(pt));

        // 1: Ustvarimo par javni-zasebni ključ
        // In damo javnega pošiljatelju
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        final KeyPair borKP = kpg.generateKeyPair();

        // 2: Ustvarimo širo in šifriramo z javnim ključem
        // Ana uporabi le Borov javni ključ
        final Cipher rsaEnc = Cipher.getInstance(algorithm);
        rsaEnc.init(Cipher.ENCRYPT_MODE, borKP.getPublic());
        final byte[] ct = rsaEnc.doFinal(pt);

        // 3: Izpišemo tajnopis
        System.out.println("CT: " + Agent.hex(ct));

        // 4: Bor dešifrira tajnopis s pomočjo zasebnega ključa
        final Cipher rsaDec = Cipher.getInstance(algorithm);
        rsaDec.init(Cipher.DECRYPT_MODE, borKP.getPrivate());
        final byte[] decryptedText = rsaDec.doFinal(ct);

        // 5: Bor izpiše sporočilo
        System.out.println("PT: " + Agent.hex(decryptedText));
        final String message2 = new String(decryptedText, StandardCharsets.UTF_8);
        System.out.println("Message: " + message2);
    }
}
