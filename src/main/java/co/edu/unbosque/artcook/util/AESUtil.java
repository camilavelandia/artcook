package co.edu.unbosque.artcook.util;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Clase de utilidad para operaciones de cifrado AES y funciones de hash.
 * Proporciona métodos para cifrar y descifrar contraseñas usando AES en modo GCM,
 * así como métodos para generar hashes usando varios algoritmos.
 */
public class AESUtil {

    /** Algoritmo de cifrado utilizado (AES). */
    private static final String ALGORITMO = "AES";

    /** Modo de cifrado y padding utilizados (AES en modo GCM sin padding). */
    private static final String TIPOCIFRADO = "AES/GCM/NoPadding";

    /**
     * Cifra un texto utilizando AES en modo GCM.
     *
     * @param llave  clave de cifrado (debe tener 16 caracteres para AES-128)
     * @param iv     vector de inicialización para el cifrado
     * @param texto  texto a cifrar
     * @return texto cifrado en formato Base64
     */
    public static String encrypt(String llave, String iv, String texto) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(TIPOCIFRADO);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        SecretKeySpec secretKeySpec = new SecretKeySpec(llave.getBytes(), ALGORITMO);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv.getBytes());
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        byte[] encrypted = null;
        try {
            encrypted = cipher.doFinal(texto.getBytes());
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return new String(encodeBase64(encrypted));
    }

    /**
     * Descifra un texto cifrado con AES en modo GCM.
     *
     * @param llave     clave de cifrado (debe ser la misma utilizada para cifrar)
     * @param iv        vector de inicialización (debe ser el mismo utilizado para cifrar)
     * @param encrypted texto cifrado en formato Base64
     * @return texto descifrado
     */
    public static String decrypt(String llave, String iv, String encrypted) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(TIPOCIFRADO);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }

        SecretKeySpec secretKeySpec = new SecretKeySpec(llave.getBytes(), ALGORITMO);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv.getBytes());
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        byte[] enc = decodeBase64(encrypted);
        byte[] decrypted = null;
        try {
            decrypted = cipher.doFinal(enc);
            return new String(decrypted);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * Descifra un texto utilizando la clave y vector de inicialización predeterminados del sistema.
     *
     * @param encrypted texto cifrado en formato Base64
     * @return texto descifrado
     */
    public static String decrypt(String encrypted) {
        String iv = "artcookappcipher";
        String key = "artcook16charkey";
        return decrypt(key, iv, encrypted);
    }

    /**
     * Cifra un texto utilizando la clave y vector de inicialización predeterminados del sistema.
     *
     * @param plainText texto a cifrar
     * @return texto cifrado en formato Base64
     */
    public static String encrypt(String plainText) {
        String iv = "artcookappcipher";
        String key = "artcook16charkey";
        return encrypt(key, iv, plainText);
    }

    /**
     * Genera un hash MD5 del contenido proporcionado.
     *
     * @param content texto a convertir en hash
     * @return representación hexadecimal del hash MD5
     */
    public static String hashingToMD5(String content) {
        return DigestUtils.md5Hex(content);
    }

    /**
     * Genera un hash SHA-1 del contenido proporcionado.
     *
     * @param content texto a convertir en hash
     * @return representación hexadecimal del hash SHA-1
     */
    public static String hashingToSHA1(String content) {
        return DigestUtils.sha1Hex(content);
    }

    /**
     * Genera un hash SHA-256 del contenido proporcionado.
     *
     * @param content texto a convertir en hash
     * @return representación hexadecimal del hash SHA-256
     */
    public static String hashingToSHA256(String content) {
        return DigestUtils.sha256Hex(content);
    }

    /**
     * Genera un hash SHA-384 del contenido proporcionado.
     *
     * @param content texto a convertir en hash
     * @return representación hexadecimal del hash SHA-384
     */
    public static String hashingToSHA384(String content) {
        return DigestUtils.sha384Hex(content);
    }

    /**
     * Genera un hash SHA-512 del contenido proporcionado.
     *
     * @param content texto a convertir en hash
     * @return representación hexadecimal del hash SHA-512
     */
    public static String hashingToSHA512(String content) {
        return DigestUtils.sha512Hex(content);
    }
}