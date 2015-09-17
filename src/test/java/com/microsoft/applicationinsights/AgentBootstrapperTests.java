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

package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.common.ApplicationInsightsSender;
import com.microsoft.applicationinsights.contracts.ContainerStateEvent;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.python.ContainerStatePythonBootstrapper;
import com.microsoft.applicationinsights.python.MetricCollectionPythonBoostrapper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private static final String PYTHON_TEST_METRIC_EVENT_SCRIPT_FILENAME = "test_metric_event.py";
    private static final String PYTHON_TEST_STATE_EVENT_SCRIPT_FILENAME = "test_state_event.py";
    private AgentBootstrapper agentBootstrapperUnderTest = new AgentBootstrapper();
    private ApplicationInsightsSender aiSenderMock;

    @Before
    public void initTest() {
        aiSenderMock = mock(ApplicationInsightsSender.class);
    }

    @Test
    public void testMetricCollectionProcess() throws InterruptedException, IOException {
        String testPythonScriptPath = getTestPythonScriptPath(PYTHON_TEST_METRIC_EVENT_SCRIPT_FILENAME);

        MetricCollectionPythonBoostrapper metricCollectionPythonBoostrapper = new MetricCollectionPythonBoostrapper("custom", "--script", testPythonScriptPath);
        Thread thread = AgentBootstrapper.createMetricCollectionProcess(aiSenderMock, metricCollectionPythonBoostrapper);

        agentBootstrapperUnderTest.run(aiSenderMock, thread, createWorklessThread(), createWorklessThread());

        verify(aiSenderMock, times(1)).track(any(ContainerStatsMetric.class));
    }

    @Test
    public void testContainerStateProcess() throws InterruptedException, IOException {
        String testPythonScriptPath = getTestPythonScriptPath(PYTHON_TEST_STATE_EVENT_SCRIPT_FILENAME);

        ContainerStatePythonBootstrapper bootstrapper = new ContainerStatePythonBootstrapper("custom", "--script", testPythonScriptPath);
        Thread thread = AgentBootstrapper.createContainerStateProcess(aiSenderMock, bootstrapper);

        agentBootstrapperUnderTest.run(aiSenderMock, createWorklessThread(), thread, createWorklessThread());

        verify(aiSenderMock, times(1)).track(any(ContainerStateEvent.class));
    }

    private String getTestPythonScriptPath(String filename) throws IOException {
        ClassLoader classLoader = AgentBootstrapperTests.class.getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream(filename);

        // ProcessBuilder cannot handle path containing spaces. Therefore, we copy the test python script into
        // a temp location and loading it from there.
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempPython = new File(tempDir, filename);
        Files.copy(resourceAsStream, tempPython.toPath(), StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Test python script has been copied to : " + tempPython);

        return tempPython.getAbsolutePath();
    }

    private Thread createWorklessThread() {
        return new Thread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }
}