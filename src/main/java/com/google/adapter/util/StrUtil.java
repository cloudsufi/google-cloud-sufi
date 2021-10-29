package com.google.adapter.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrUtil {

    static final char[] hex = new char[] {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
            'B', 'C', 'D', 'E', 'F'};
    private static final Pattern NON_ENCODED_SPL_CHARS_PATTERN = Pattern.compile("[<>'\"]");
    private static final StringBuilder CHAR_BUILDER =
            new StringBuilder(28).append("amp; &quot; &apos; &lt; &gt;");

    /**
     * Removes non encoded but invalid characters from the input {@code str}. And encodes following
     * non encoded but invalid XML characters:<br>
     * {@code "} to {@code &quot;}<br>
     * {@code '} to {@code &apos;}<br>
     * {@code <} to {@code &lt;} and<br>
     * {@code >} to {@code &gt;}
     *
     * @param str String value
     * @param isInbound Is Inbound or Outbound
     * @return Modified String
     */
    public static StringBuilder removeEncodeInvalidControlChars(String str, boolean isInbound) {
        StringBuilder sb = null;
        // 25175853-RT:NPE while sending spcl characters for SAP Adapter IDOC outbound
        if (str != null && !str.isEmpty()) {
            final int length = str.length();
            Matcher splCharMatcher = NON_ENCODED_SPL_CHARS_PATTERN.matcher(str);
            if (splCharMatcher.matches())
                sb = new StringBuilder(length + 200);
            else
                sb = new StringBuilder(length);
            char[] newChars = str.toCharArray();
            for (int j = 0; j < length; j++) {
                int ch = (int) newChars[j];
                boolean isValid = isValid(ch);
                if (isValid && isInbound) {
                    switch (ch) { // Switch added to fix regression due to non-handling of special
                        // meaning chars
                        // (", ', <, >)
                        case 34:
                            sb.append(CHAR_BUILDER, 5, 11);
                            break;
                        case 39:
                            sb.append(CHAR_BUILDER, 12, 18);
                            break;
                        case 60:
                            sb.append(CHAR_BUILDER, 19, 23);
                            break;
                        case 62:
                            sb.append(CHAR_BUILDER, 24, 28);
                            break;
                        default:
                            sb.append((char) ch);
                    }
                } else if (isValid && !isInbound)
                    sb.append((char) ch);
            }
            sb.trimToSize();
        }
        return sb;
    }

    /**
     * Checks if the number exists inside following ranges (both lower and upper bound included) of
     * valid characters. 9 - 10, 13, 32 - 55295, 57344 - 65533, 65536 - 1114111
     *
     * @param num
     * @return
     */
    private static boolean isValid(int num) {
        if (num < 55296) {
            if (31 < num)
                return true;
            return (num == 9 || num == 10 || num == 13);
        } else if (num < 65534) {
            return 57343 < num;
        } else if (65535 < num) {
            return num < 1114112;
        }
        return false;
    }

    public static String encodeName(String s) {
        if (s == null) {
            return null;
        }
        int len = s.length();
        if (len < 1) {
            return s;
        }
        char[] buf = new char[len * 5];
        int idx = 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) || (c == '_')) {
                buf[(idx++)] = c;
            } else if (((c >= '0') && (c <= '9')) || (c == '.')) { // OK w/Numbers
                if (i == 0) {
                    buf[(idx++)] = '_';
                    buf[(idx++)] = '-';
                    buf[(idx++)] = '-';
                    buf[(idx++)] = '3';
                }
                buf[(idx++)] = c;
            } else if (c == '/') {
                buf[(idx++)] = '_';
                buf[(idx++)] = '-';
            } else {
                buf[(idx++)] = '_';
                buf[(idx++)] = '-';
                buf[(idx++)] = '-';
                buf[(idx++)] = hex[(c >> '\004' & 0xF)];
                buf[(idx++)] = hex[(c >> '\000' & 0xF)];
            }
        }
        return new String(buf, 0, idx);
    }

    public static boolean fileFinder(String dirName, String fileFilter){
        File dir = new File(dirName);
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename)
            { return filename.startsWith(fileFilter) && filename.endsWith(".json"); }
        }).length > 0;
    }
}
