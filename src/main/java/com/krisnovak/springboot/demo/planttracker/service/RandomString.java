package com.krisnovak.springboot.demo.planttracker.service;

import java.security.SecureRandom;

/**
 * Class used to generate a secure random string
 */
public class RandomString {
    /**
     * Function that generates a secure random string according to the provided length
     * @param length The length of the random string to create
     * @return A secure random string
     */
    public static String generateRandomString(int length){
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!".toCharArray();
        StringBuilder sb = new StringBuilder(length);
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < length; i++) {
            sb.append(chars[random.nextInt(chars.length)]);
        }

        return sb.toString();
    }
}
