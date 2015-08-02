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
