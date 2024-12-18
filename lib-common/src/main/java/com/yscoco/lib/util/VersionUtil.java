package com.yscoco.lib.util;

public class VersionUtil {
    public static int compareVersions(String version1, String version2) {
        String[] v1 = version1.split("\\.");
        String[] v2 = version2.split("\\.");
        int length = Math.max(v1.length, v2.length);
        for (int i = 0; i < length; i++) {
            int num1 = (i < v1.length) ? Integer.parseInt(v1[i]) : 0;
            int num2 = (i < v2.length) ? Integer.parseInt(v2[i]) : 0;

            if (num1 < num2) {
                return -1;
            } else if (num1 > num2) {
                return 1;
            }
        }
        return 0;
    }

    public static boolean isValidVersion(String version) {
        if (version == null) {
            return false;
        }
        String[] segments = version.split("\\.");
        for (String segment : segments) {
            if (!segment.matches("\\d+")) {
                return false;
            }
        }
        return true;
    }
}
