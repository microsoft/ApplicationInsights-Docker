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