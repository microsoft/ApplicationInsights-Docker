package com.microsoft.applicationinsights.contracts;

import com.microsoft.applicationinsights.common.TestConstants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by yonisha on 8/6/2015.
 */
public class ContainerStateEventTests {
    @Test
    public void testStateEventJsonParsedSuccessfully() {
        ContainerStateEvent containerStateEvent = new ContainerStateEvent(TestConstants.DEFAULT_STATE_EVENT);

        Assert.assertEquals("docker-container-state", containerStateEvent.getName());
        Assert.assertEquals("ubuntu", containerStateEvent.getProperties().get("docker-image"));
    }
}
