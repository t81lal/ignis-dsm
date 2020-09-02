package com.bilbtopia.forager.dsm.util;

public class StringUtil {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String javaEscapeString(String str) {
        return str.replace("\\", "\\\\").replace("\t", "\\t").replace("\b", "\\b").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\f", "\\f").replace("\'", "\\'").replace("\"", "\\\"");
    }

    public static String javaEscapeCharacter(char c) {
        if (c == '\'') {
            return "\\'";
        } else if (c == '\\') {
            return "\\\\";
        } else if (c == '\t') {
            return "\\t";
        } else if (c == '\n') {
            return "\\n";
        } else if (c == '\r') {
            return "\\r";
        } else if (c == '\f') {
            return "\\f";
        } else if (c >= 33 && c <= 126) {
            return Character.toString(c);
        } else {
            return "\\u" + Integer.toHexString((int) c);
        }
    }

    public static String getSimpleKlassName(String fullPackageKlass) {
        String[] parts = fullPackageKlass.split("/");
        return parts[parts.length - 1];
    }

    public static String getInnerKlassName(String fullPackageKlass) {
        String klassName = getSimpleKlassName(fullPackageKlass);
        if (!klassName.contains("$")) {
            throw new IllegalArgumentException("Not inner class name");
        }
        String[] innerParts = klassName.split("\\$");
        return innerParts[innerParts.length - 1];
    }
}
