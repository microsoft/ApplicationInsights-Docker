package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.agent.ApplicationInsightsSender;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.python.MetricCollectionPythonBoostrapper;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URL;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by yonisha on 8/3/2015.
 */
public class AgentBootstrapperTests {
    private static final String PYTHON_TEST_SCRIPT_FILENAME = "test_metric.py";
    private static MetricCollectionPythonBoostrapper metricCollectionPythonBoostrapper;
    private AgentBootstrapper agentBootstrapperUnderTest = new AgentBootstrapper();
    private ApplicationInsightsSender aiSenderMock = mock(ApplicationInsightsSender.class);

    @BeforeClass
    public static void initClass() {
        ClassLoader classLoader = AgentBootstrapperTests.class.getClassLoader();
        URL resource = classLoader.getResource(PYTHON_TEST_SCRIPT_FILENAME);

        Assert.assertNotNull(resource);

        String testScriptPath = resource.getPath().replaceFirst("/", "");

        metricCollectionPythonBoostrapper = new MetricCollectionPythonBoostrapper("custom", "--script", testScriptPath);
    }

    @Test
    public void testAgentBootrapperStartsMetricCollectionSuccessfully() throws InterruptedException {
        Thread thread = agentBootstrapperUnderTest.executeMetricCollectionProcess(aiSenderMock, metricCollectionPythonBoostrapper);

        thread.join(2000);

        verify(aiSenderMock, times(1)).sentMetric(any(ContainerStatsMetric.class));
    }
}
