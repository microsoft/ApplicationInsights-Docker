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

package com.microsoft.applicationinsights.python;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by yonisha on 7/26/2015.
 */
public class MetricProviderPythonBootstrapperTests {

    private com.microsoft.applicationinsights.python.ProcessBuilder processBuilderMock = mock(ProcessBuilder.class);
    private Process processMock = mock(Process.class);
    private PythonBootstrapper bootstrapperUnderTest = new MetricCollectionPythonBoostrapper(processBuilderMock);
    private BufferedInputStream inputStreamMock = mock(BufferedInputStream.class);

    @Before
    public void testInit() throws IOException {
        when(processBuilderMock.start()).thenReturn(processMock);
        when(processMock.getInputStream()).thenReturn(inputStreamMock);
        this.bootstrapperUnderTest = new MetricCollectionPythonBoostrapper(processBuilderMock);
        this.bootstrapperUnderTest.start(false);
    }

    @Test
    public void testIsAliveReturnsTrueWhenProcessAlive() throws IOException {
        when(processMock.exitValue()).thenThrow(new java.lang.IllegalThreadStateException());

        Assert.assertTrue(bootstrapperUnderTest.isAlive());
    }

    @Test
    public void testIsAliveReturnsFalseProcessNotStartedYet() {
        Assert.assertFalse(bootstrapperUnderTest.isAlive());
    }

    @Test
    public void testIsAliveReturnsFalseWhenProcessExisted() {
        when(processMock.exitValue()).thenReturn(0);

        Assert.assertFalse(bootstrapperUnderTest.isAlive());
    }

    @Test
    public void testBoostrapperStartsNewProcess() throws IOException {
        verify(processBuilderMock, times(1)).start();
    }

    @Test
    public void testPreviousProcessResourcesAreClosed() throws IOException {
        bootstrapperUnderTest.start(false);

        verify(inputStreamMock, times(1)).close();
    }
}
