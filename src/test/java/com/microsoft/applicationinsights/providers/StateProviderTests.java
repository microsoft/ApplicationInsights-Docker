package com.microsoft.applicationinsights.providers;

import com.microsoft.applicationinsights.common.TestConstants;
import com.microsoft.applicationinsights.contracts.ContainerStateEvent;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by yonisha on 8/5/2015.
 */
public class StateProviderTests {

    @Test
    public void testValidJsonResultsWithContainerStateEvent() throws IOException {
        ContainerStateEvent event = createProviderAndGetState(TestConstants.DEFAULT_STATE_EVENT);

        Assert.assertNotNull(event);
    }

    @Test
    public void testCorruptedJsonNotThrowException() throws IOException {
        ContainerStateEvent event = createProviderAndGetState(" { some corrupted json } ");

        Assert.assertNull(event);
    }

    private ContainerStateEvent createProviderAndGetState(String providerInputString) throws IOException {
        BufferedReader inputBuffer = new BufferedReader(new StringReader(providerInputString));

        StateProvider provider = new StateProvider(inputBuffer);
        return provider.getNext();
    }
}
