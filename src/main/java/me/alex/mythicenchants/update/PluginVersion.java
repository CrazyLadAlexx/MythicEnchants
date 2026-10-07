package me.alex.mythicenchants.update;

import java.math.BigInteger;
import java.util.regex.Pattern;

public record PluginVersion(BigInteger major, BigInteger minor, BigInteger patch) implements Comparable<PluginVersion> {
    private static final Pattern FORMAT = Pattern.compile("v?(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)");
    public static PluginVersion parse(String raw) {
        var matcher = FORMAT.matcher(raw.trim());
        if (!matcher.matches()) throw new IllegalArgumentException("Version must be major.minor.patch, optionally prefixed with v");
        return new PluginVersion(new BigInteger(matcher.group(1)), new BigInteger(matcher.group(2)), new BigInteger(matcher.group(3)));
    }
    @Override public int compareTo(PluginVersion other) {
        int value = major.compareTo(other.major);
        if (value == 0) value = minor.compareTo(other.minor);
        if (value == 0) value = patch.compareTo(other.patch);
        return value;
    }
    @Override public String toString() { return major + "." + minor + "." + patch; }
}
