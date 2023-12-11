package fri.vp;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.HexFormat;

public class KeyDerivation {
    public static void main(String[] args) throws Exception {
        // geslo
        final String password = "hunter2";

        // naključna, javna in nato statična vrednost
        final byte[] salt = new byte[16];
        SecureRandom.getInstanceStrong().nextBytes(salt);

        // ali če moramo ključ izpeljati z uporabe konkretne vrednosti soli
        // final byte[] salt = HexFormat.of().parseHex("e24701ca2d923d43e50a405ec718f3af");

        // Uporabimo PBKDF2 z HMAC-SHA256, dodamo sol, povemo število iteracij in želeno dolžino ključa
        final SecretKeyFactory pbkdf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        final KeySpec specs = new PBEKeySpec(password.toCharArray(), salt, 1000000, 128);
        final Key key = pbkdf.generateSecret(specs);

        System.out.printf("key = %s%n", HexFormat.of().formatHex(key.getEncoded()));
        System.out.printf("len(key) = %d bytes%n", key.getEncoded().length);
    }
}
