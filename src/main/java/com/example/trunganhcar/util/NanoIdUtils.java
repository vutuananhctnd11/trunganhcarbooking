package com.example.trunganhcar.util;

import java.security.SecureRandom;

public final class NanoIdUtils {

    private static final SecureRandom RANDOM = new SecureRandom();
    // Bảng ký tự URL-friendly không gây nhầm lẫn: loại bỏ 0, O, I, l
    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    public static final int DEFAULT_SIZE = 10;

    private NanoIdUtils() {}

    public static String randomNanoId() {
        return randomNanoId(DEFAULT_SIZE);
    }

    public static String randomNanoId(int size) {
        StringBuilder builder = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            int index = RANDOM.nextInt(ALPHABET.length);
            builder.append(ALPHABET[index]);
        }
        return builder.toString();
    }
}

