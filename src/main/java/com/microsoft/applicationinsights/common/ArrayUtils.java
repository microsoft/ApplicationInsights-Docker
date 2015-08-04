package com.microsoft.applicationinsights.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by yonisha on 8/3/2015.
 */
public class ArrayUtils {
    public static String[] addFirst(String string, String[] current) {
        List<String> strings = new ArrayList<String>(Arrays.asList(current));
        strings.add(0, string);

        return strings.toArray(new String[strings.size()]);
    }
}
