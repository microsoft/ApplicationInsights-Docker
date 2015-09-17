/*
 * ApplicationInsights-Docker
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
