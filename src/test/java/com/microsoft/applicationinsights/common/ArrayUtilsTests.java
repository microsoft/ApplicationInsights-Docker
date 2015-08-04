package com.microsoft.applicationinsights.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by yonisha on 8/3/2015.
 */
public class ArrayUtilsTests {
    @Test
    public void testStringAddedFirstSuccessfully() {
        String newString = "some_string";
        String[] array = {"first", "second"};

        String[] updatedArray = ArrayUtils.addFirst(newString, array);

        Assert.assertEquals(newString, updatedArray[0]);
    }
}
