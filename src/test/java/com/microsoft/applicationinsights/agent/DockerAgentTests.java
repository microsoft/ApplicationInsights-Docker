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

package com.microsoft.applicationinsights.agent;

import com.microsoft.applicationinsights.common.ApplicationInsightsSender;
import com.microsoft.applicationinsights.common.TestConstants;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.providers.MetricProvider;
import com.microsoft.applicationinsights.python.PythonBootstrapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import static org.mockito.Mockito.*;

/**
 * Created by yonisha on 7/26/2015.
 */
public class DockerAgentTests {
    private ApplicationInsightsSender applicationInsightsSender;
    private PythonBootstrapper pythonBootstrapper;
    private DockerAgent agentUnderTest;
    private MetricProvider metricProviderMock;

    @Before
    public void testInit() throws IOException {
        initializeMocks();

        agentUnderTest.run();
    }

    @Test
    public void testWhenAgentStartPythonBoostrapperStarts() throws IOException {
        verify(pythonBootstrapper, atLeastOnce()).start(false);
    }

    @Test
    public void testWhenAgentStartsMetricsBeingCollected() throws IOException {
        verify(metricProviderMock, atLeastOnce()).getNext();
    }

    @Test
    public void testMetricIsSent() {
        verify(this.applicationInsightsSender, atLeastOnce()).track(any(ContainerStatsMetric.class));
    }

    @Test
    public void testOnlyOnePythonProcessStarts() throws IOException {
        verify(this.pythonBootstrapper, times(1)).start(false);
    }

    @Test
    public void testWhenMetricProviderReturnsNullAndPythonProcessAliveNoNewPythonProcessIsStarted() throws IOException {
        initializeMocks();

        final int[] numOfTrackedEvents = new int[1];
        when(this.metricProviderMock.getNext()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                numOfTrackedEvents[0] = numOfTrackedEvents[0] + 1;
                if (numOfTrackedEvents[0] >= 3) {
                    agentUnderTest.stop();
                }

                return numOfTrackedEvents[0] == 1 || numOfTrackedEvents[0] == 3 ? new ContainerStatsMetric(TestConstants.DEFAULT_METRIC_EVENT) : null;
            }
        });

        when(this.pythonBootstrapper.isAlive()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return numOfTrackedEvents[0] <= 3;
            }
        });

        this.agentUnderTest.run();

        verify(this.pythonBootstrapper, times(1)).start(false);
        verify(this.metricProviderMock, times(4)).getNext();
        verify(this.applicationInsightsSender, times(2)).track(any(ContainerStatsMetric.class));
    }

    @Test
    public void testWhenPythonProcessNotAliveThenNewProcessIsStarted() throws IOException {
        initializeMocks();

        final int[] numOfTrackedEvents = new int[1];
        when(this.metricProviderMock.getNext()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                numOfTrackedEvents[0] = numOfTrackedEvents[0] + 1;
                if (numOfTrackedEvents[0] == 2) {
                    agentUnderTest.stop();
                }

                return null;
            }
        });

        this.agentUnderTest.run();

        verify(this.pythonBootstrapper, times(2)).start(false);
    }

    private void initializeMocks() throws IOException {
        final int numberOfEventsToSend = 5;
        final int[] numOfTrackedEvents = new int[1];

        this.metricProviderMock = mock(MetricProvider.class);
        when(this.metricProviderMock.getNext()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                numOfTrackedEvents[0] = numOfTrackedEvents[0] + 1;
                if (numOfTrackedEvents[0] == numberOfEventsToSend) {
                    agentUnderTest.stop();
                }

                return numOfTrackedEvents[0] <= numberOfEventsToSend ? new ContainerStatsMetric(TestConstants.DEFAULT_METRIC_EVENT) : null;
            }
        });

        this.applicationInsightsSender = mock(ApplicationInsightsSender.class);

        this.pythonBootstrapper = mock(PythonBootstrapper.class);
        when(this.pythonBootstrapper.getResult()).thenReturn(this.metricProviderMock);
        when(this.pythonBootstrapper.isAlive()).thenReturn(false);

        // 4 has no specific meaning here, just requires exit value != 0.
        when(this.pythonBootstrapper.getExitValue()).thenReturn(4);

        this.agentUnderTest = new DockerAgent(pythonBootstrapper, applicationInsightsSender);
    }
}