package com.microsoft.applicationinsights.agent;

import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;

import java.io.IOException;

/**
 * Created by yonisha on 7/23/2015.
 */
public class DockerAgent {

    private static final String PYTHON_BOOTSTRAP_SCRIPT = "python/bootstrap.py";
    private String instrumentationKey;

    // region Ctor

    public DockerAgent(String instrumentationKey) {
        this.instrumentationKey = instrumentationKey;
    }

    // endregion Ctor

    // region Public

    public void run() throws IOException {
        System.out.println("Starting Python bootsrapper");
        PythonBootstrapper pythonBootstrapper = new PythonBootstrapper(PYTHON_BOOTSTRAP_SCRIPT);
        MetricProvider metricProvider = pythonBootstrapper.start();
        System.out.println("Python process is running.");

        ApplicationInsightsSender applicationInsightsSender = new ApplicationInsightsSender(this.instrumentationKey);

        collectMetrics(metricProvider, applicationInsightsSender);
    }

    // endregion Public

    // region Private

    private static void collectMetrics(MetricProvider metricProvider, ApplicationInsightsSender applicationInsightsSender) {
        System.out.println("Starting to collect metrics");

        while (true) {
            ContainerStatsMetric metric = getMetric(metricProvider);

            if (metric == null) {
                continue;
            }

            applicationInsightsSender.sentMetric(metric);
        }
    }

    private static ContainerStatsMetric getMetric(MetricProvider metricProvider) {
        ContainerStatsMetric nextMetric = null;

        try {
            nextMetric = metricProvider.getNext();
        } catch (IOException e) {
            System.out.println("Failed to fetch metric with error: " + e.getMessage());
        }

        return nextMetric;
    }

    // endregion Private
}
