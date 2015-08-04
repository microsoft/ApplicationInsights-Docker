package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.agent.ApplicationInsightsSender;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.python.MetricCollectionPythonBoostrapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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
    public static void initClass() throws URISyntaxException, IOException {
        ClassLoader classLoader = AgentBootstrapperTests.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream(PYTHON_TEST_SCRIPT_FILENAME);

        // ProcessBuilder cannot handle path containing spaces. Therefore, we copy the test python script into
        // a temp location and loading it from there.
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempPython = new File(tempDir, PYTHON_TEST_SCRIPT_FILENAME);
        Files.copy(resourceAsStream, tempPython.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Test python script copied to : " + tempPython);

        metricCollectionPythonBoostrapper = new MetricCollectionPythonBoostrapper("custom", "--script", tempPython.getAbsolutePath());
    }

    @Test
    public void testAgentBootrapperStartsMetricCollectionSuccessfully() throws InterruptedException {
        Thread thread = agentBootstrapperUnderTest.executeMetricCollectionProcess(aiSenderMock, metricCollectionPythonBoostrapper);

        thread.join(3000);

        verify(aiSenderMock, times(1)).sentMetric(any(ContainerStatsMetric.class));
    }
}
