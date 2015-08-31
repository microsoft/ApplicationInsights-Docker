package com.microsoft.applicationinsights.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by yonisha on 8/31/2015.
 */
public class StringUtilsTests {

    @Test
    public void testEmptyStringReturnsTrue() {
        Assert.assertTrue(StringUtils.isNullOrEmpty(""));
    }

    @Test
    public void testNullStringReturnsTrue() {
        Assert.assertTrue(StringUtils.isNullOrEmpty(null));
    }
}
