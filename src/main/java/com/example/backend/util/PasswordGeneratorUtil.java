package com.example.backend.util;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class PasswordGeneratorUtil {

    private static final String CHAR_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPERCASE = CHAR_LOWERCASE.toUpperCase();
    private static final String DIGIT = "0123456789";
    private static final int PASSWORD_LENGTH = 8;

    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWERCASE + CHAR_UPPERCASE + DIGIT;
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomPassword() {
        // 1 uppercase, 1 digit, 6 random from all categories
        Stream<Character> tempStream = Stream.concat(
                random.ints(1, 0, CHAR_UPPERCASE.length()).mapToObj(CHAR_UPPERCASE::charAt),
                random.ints(1, 0, DIGIT.length()).mapToObj(DIGIT::charAt)
        );

        Stream<Character> passwordStream = Stream.concat(
                tempStream,
                random.ints(PASSWORD_LENGTH - 2, 0, PASSWORD_ALLOW_BASE.length()).mapToObj(PASSWORD_ALLOW_BASE::charAt)
        );

        List<Character> passwordChars = passwordStream.collect(Collectors.toList());
        Collections.shuffle(passwordChars);

        return passwordChars.stream()
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
