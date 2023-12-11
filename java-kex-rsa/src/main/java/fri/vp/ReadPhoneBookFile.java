package fri.vp;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class ReadPhoneBookFile {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        final byte[] bytes = Files.readAllBytes(Path.of("../data/phonebook.bin"));
        final byte[] salt = Arrays.copyOfRange(bytes, 0, 16);
        final byte[] iv = Arrays.copyOfRange(bytes, 16, 32);
        final byte[] ct = Arrays.copyOfRange(bytes, 32, bytes.length);

        final Key key = deriveKey("hunter2", salt, 1000000);

        final Cipher aes = Cipher.getInstance("AES/GCM/NoPadding");
        aes.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        final byte[] pt = aes.doFinal(ct);

        System.out.println(new String(pt, StandardCharsets.UTF_8));
    }

    public static Key deriveKey(String password, byte[] salt, int iterations) throws NoSuchAlgorithmException, InvalidKeySpecException {
        final KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, 128);
        final SecretKeyFactory pbKDF = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        return new SecretKeySpec(pbKDF.generateSecret(spec).getEncoded(), "AES");
    }
}
