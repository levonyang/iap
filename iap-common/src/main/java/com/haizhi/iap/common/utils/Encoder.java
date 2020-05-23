package com.haizhi.iap.common.utils;

import java.util.UUID;

public class Encoder {

    private static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int MAX_RADIX = ALPHABET.length();

    private Encoder() {
    }

    public static String encode(long num) {
        num = Math.abs(num);

        StringBuilder sb = new StringBuilder();
        while (num > 0) {
            sb.append(ALPHABET.charAt((int) (num % MAX_RADIX)));
            num /= MAX_RADIX;
        }
        return sb.reverse().toString();
    }

    public static long decode(String str) {
        long num = 0;
        for (int i = 0; i < str.length(); i++)
            num = num * MAX_RADIX + ALPHABET.indexOf(str.charAt(i));
        return num;
    }

    public static String random() {
        return encode(
                UUID.randomUUID().getMostSignificantBits()
        );
    }
}
