package me.alex.mythicenchants.util;

public final class RomanNumerals {
    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
    private RomanNumerals() {}
    public static String format(int number) {
        if (number < 1 || number > 3999) throw new IllegalArgumentException("Level must be between 1 and 3999");
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < VALUES.length; i++) {
            while (number >= VALUES[i]) { result.append(SYMBOLS[i]); number -= VALUES[i]; }
        }
        return result.toString();
    }
}
