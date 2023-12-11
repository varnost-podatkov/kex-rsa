package fri.vp;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HexFormat;

public class GCMExample {
    public static void main(String[] args) throws Exception {
        // Ustvarimo sporočilo in ga postrojimo z UTF8
        final String message = "Moje sporočilo.";
        final byte[] pt = message.getBytes(StandardCharsets.UTF_8);

        System.out.printf("Pošiljam: '%s'%n", new String(pt, StandardCharsets.UTF_8));

        // Ustvarimo naključen ključ
        final Key key = KeyGenerator.getInstance("AES").generateKey();

        // Ustvarimo algoritem AES/GCM/NoPadding
        final Cipher ana = Cipher.getInstance("AES/GCM/NoPadding");
        ana.init(Cipher.ENCRYPT_MODE, key); // pri tem klicu se IV samodejno izbere
        final byte[] ct = ana.doFinal(pt); // šifriramo
        final byte[] iv = ana.getIV(); // preberemo IV

        // Šestnajstiški izpis čistopisa, vrednosti IV in tajnopisa
        System.out.println("PT: " + HexFormat.of().formatHex(pt));
        System.out.println("IV: " + HexFormat.of().formatHex(iv));
        System.out.println("CT: " + HexFormat.of().formatHex(ct));

        // Prejemniku pošljemo IV in CT
        // Odkomentirajte spodnjo vrstico, da preverite, kaj se zgodi, če se tajnopis spremeni
        // ct[0] ^= 1;

        // Prejemnik Bor prav tako ustvari enak algoritem AES/GCM/NoPadding
        final Cipher bor = Cipher.getInstance("AES/GCM/NoPadding");
        // kot tretji parameter pri inicializaciji podamo instanco GCMParameterSpec(dolzina_značke, IV)
        bor.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        final byte[] dt = bor.doFinal(ct); // dešifriramo
        System.out.println("PT: " + HexFormat.of().formatHex(dt)); // izpišemo dešifrirano vrednost
        // Izpišemo sporočilo
        System.out.printf("Prejel sem: '%s'%n", new String(dt, StandardCharsets.UTF_8));
    }
}
