package fri.vp;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class Fernet {

    public static byte[] genKey() throws NoSuchAlgorithmException {
        final byte[] key = new byte[32];
        SecureRandom.getInstanceStrong().nextBytes(key);
        return Base64.getUrlEncoder().encode(key);
    }

    public static byte[] decrypt(byte[] key, byte[] ct) throws NoSuchAlgorithmException, InvalidKeyException,
            NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        // unpack keys
        final byte[] decodedKey = Base64.getUrlDecoder().decode(key);

        // create keys
        final Key macKey = new SecretKeySpec(Arrays.copyOfRange(decodedKey, 0, 16), "HmacSHA256");
        final Key encKey = new SecretKeySpec(Arrays.copyOfRange(decodedKey, 16, 32), "AES");

        // load ciphertext into bytebuffer
        final byte[] decodedCt = Base64.getUrlDecoder().decode(ct);
        final ByteBuffer buff = ByteBuffer.wrap(decodedCt);

        // check version
        final int version = buff.get();
        assert version == (byte) 128;

        // check timestamp
        final long timestamp = buff.getLong();
        System.out.println(new Date(timestamp * 1000));

        // get IV
        final byte[] iv = new byte[16];
        buff.get(iv, 0, 16);

        // get ct
        final byte[] ctBytes = new byte[buff.remaining() - 32];
        buff.get(ctBytes, 0, ctBytes.length);

        // mac tag
        final byte[] tag = new byte[32];
        buff.get(tag, 0, 32);

        // check MAC
        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(macKey);
        final byte[] recomputedTag = mac.doFinal(buff.slice(0, decodedCt.length - 32).array());
        assert MessageDigest.isEqual(tag, recomputedTag);

        // decrypt
        final Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aes.init(Cipher.DECRYPT_MODE, encKey, new IvParameterSpec(iv));
        return aes.doFinal(ctBytes);
    }

    public static byte[] encrypt(byte[] key, byte[] pt) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // unpack keys
        final byte[] decodedKey = Base64.getUrlDecoder().decode(key);

        // create keys
        final Key macKey = new SecretKeySpec(Arrays.copyOfRange(decodedKey, 0, 16), "HmacSHA256");
        final Key encKey = new SecretKeySpec(Arrays.copyOfRange(decodedKey, 16, 32), "AES");

        // set version
        final byte version = (byte) 128;

        // check timestamp
        final byte[] timestamp = ByteBuffer.allocate(8).putLong(System.currentTimeMillis() / 1000L).array();

        // encrypt
        final Cipher aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aes.init(Cipher.ENCRYPT_MODE, encKey);
        final byte[] ct = aes.doFinal(pt);

        // get IV
        final byte[] iv = aes.getIV();

        // mac tag
        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(macKey);
        final byte[] tag = mac.doFinal(
                ByteBuffer.allocate(1 + timestamp.length + iv.length + ct.length)
                        .put(version)
                        .put(timestamp)
                        .put(iv)
                        .put(ct)
                        .array()
        );

        final byte[] bytes = ByteBuffer.allocate(1 + timestamp.length + iv.length + ct.length + tag.length)
                .put(version)
                .put(timestamp)
                .put(iv)
                .put(ct)
                .put(tag)
                .array();

        return Base64.getUrlEncoder().encode(bytes);
    }

    public static void main(String[] args) throws Exception {
        // Preberi Python primer
        final byte[] keyPy = Files.readAllBytes(Path.of("../fernet.key"));
        final byte[] ctPy = Files.readAllBytes(Path.of("../fernet.ct"));
        System.out.println(new String(decrypt(keyPy, ctPy), StandardCharsets.UTF_8));

        // Napravi nov primer, ga shrani in preveri v Pythonu
        final byte[] keyJava = genKey();
        final byte[] ptJava = "Pozdravljen Svet. Tole je bilo šifrirano v Javi.".getBytes(StandardCharsets.UTF_8);
        final byte[] ctJava = encrypt(keyJava, ptJava);
        Files.write(Path.of("../fernet-java.key"), keyJava);
        Files.write(Path.of("../fernet-java.ct"), ctJava);
        System.out.println(new String(decrypt(keyJava, ctJava), StandardCharsets.UTF_8));
    }
}
