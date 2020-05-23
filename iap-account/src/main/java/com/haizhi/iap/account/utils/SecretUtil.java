package com.haizhi.iap.account.utils;

import de.rtner.misc.BinTools;
import de.rtner.security.auth.spi.PBKDF2Engine;
import de.rtner.security.auth.spi.PBKDF2Parameters;

import java.security.MessageDigest;
import java.util.Random;

public class SecretUtil {

    private static final String ENCRYPT_SEPERATOR = "$";
    private static Integer DEFAULT_SALT_LEN = 8;
    private static Integer DEFAULT_PWD_LEN = 6;
    private static Integer DEFAULT_PBKDF2_ITERATIONS = 1000;
    private static EncryptMethod DEFAULT_ENCRYPT_METHOD = EncryptMethod.PBKDF2_SHA1;
    private static final String SALT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static Random random = new Random();

    public static String genSalt(Integer saltLen) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < saltLen; i++) {
            int index = random.nextInt(SALT_CHARS.length());
            sb.append(SALT_CHARS.charAt(index));
        }
        return sb.toString();
    }

    public static String genPwd() {
        return genPwd(DEFAULT_PWD_LEN);
    }

    public static String genPwd(Integer pwdLen) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < pwdLen; i++) {
            int index = random.nextInt(SALT_CHARS.length());
            sb.append(SALT_CHARS.charAt(index));
        }
        return sb.toString();
    }

    public static String genHashPassword(String password) {
        return genHashPassword(password, null, null, null);
    }

    public static String genHashPassword(String password, EncryptMethod method, Integer saltLen, Integer iterationCount) {
        if (password == null || password.equals("")) {
            throw new IllegalArgumentException("password must not be empty!");
        }

        if (method == null) {
            method = DEFAULT_ENCRYPT_METHOD;
        }

        if (saltLen == null) {
            saltLen = DEFAULT_SALT_LEN;
        }

        if (iterationCount == null) {
            iterationCount = DEFAULT_PBKDF2_ITERATIONS;
        }

        String salt = genSalt(saltLen);
        String hashAlgorithm = "Hmac" + method.getMethod().split(":")[1].toUpperCase();
        PBKDF2Parameters p = new PBKDF2Parameters(hashAlgorithm, "UTF-8", salt.getBytes(), iterationCount);
        byte[] dk = new PBKDF2Engine(p).deriveKey(password);
        String encryptPass = BinTools.bin2hex(dk).toLowerCase();

        //pbkdf2:sha1:1000$tyQyP5jh$4dfc20ce4392702397fe77b3f9518f8690c3e5e8
        StringBuffer sb = new StringBuffer();
        sb.append(method.getMethod())
                .append(":")
                .append(iterationCount)
                .append(ENCRYPT_SEPERATOR)
                .append(salt)
                .append(ENCRYPT_SEPERATOR)
                .append(encryptPass);
        return sb.toString();
    }

    public static boolean checkHashPassword(String password, String encryption) {
        if (encryption.lastIndexOf(ENCRYPT_SEPERATOR) < 0 || encryption.split("\\" + ENCRYPT_SEPERATOR).length < 3) {
            //不是系统所用的密码
            return false;
        }

        String encryptPass = encryption.substring(encryption.lastIndexOf("$") + 1, encryption.length());

        String[] seq = encryption.split("\\" + ENCRYPT_SEPERATOR);
        String hashAlgorithm = "Hmac" + seq[0].substring(seq[0].indexOf(":") + 1, seq[0].lastIndexOf(":")).toUpperCase();
        Integer iterationCount = Integer.valueOf(seq[0].substring(seq[0].lastIndexOf(":") + 1, seq[0].length()));
        String salt = seq[1];
        PBKDF2Parameters p = new PBKDF2Parameters(hashAlgorithm, "UTF-8", salt.getBytes(), iterationCount);
        String expected = BinTools.bin2hex(new PBKDF2Engine(p).deriveKey(password)).toLowerCase();
        return encryptPass.equals(expected);
    }

    public static void main(String[] args) {
        System.out.println(checkHashPassword("chen.743967", "pbkdf2:sha1:1000$tyQyP5jh$4dfc20ce4392702397fe77b3f9518f8690c3e5e8"));
//        PBKDF2Parameters p = new PBKDF2Parameters("HmacSHA256", "UTF-8", "tyQyP5jh".getBytes(), 2000);
//        byte[] dk = new PBKDF2Engine(p).deriveKey("Hello World");
//        System.out.println(BinTools.bin2hex(dk));
    }

}

enum EncryptMethod {

    PBKDF2_SHA1("pbkdf2:sha1"),
    PBKDF2_SHA224("pbkdf2:sha224"),
    PBKDF2_SHA256("pbkdf2:sha256"),
    PBKDF2_SHA384("pbkdf2:sha384"),
    PBKDF2_SHA512("pbkdf2:sha512"),
    PBKDF2_MD5("pbkdf2:md5");

    String method;

    EncryptMethod(String method) {
        this.method = method;
    }

    public String getMethod() {
        return this.method;
    }
}
