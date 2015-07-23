package com.microsoft.applicationinsights.agent;

import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;

import java.io.IOException;

/**
 * Created by yonisha on 7/22/2015.
 *
 * The agent is executed by the Docker ENTRYPOINT command.
 */
public class DockerAgent {
    private static final String PYTHON_BOOTSTRAP_SCRIPT = "python/bootstrap.py";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Instrumentation key required.");

            return;
        }

        System.out.println("Starting Python bootsrapper");
        PythonBootstrapper pythonBootstrapper = new PythonBootstrapper(PYTHON_BOOTSTRAP_SCRIPT);
        MetricProvider metricProvider = pythonBootstrapper.start();

        String instrumentationKey = args[0];
        ApplicationInsightsSender applicationInsightsSender = new ApplicationInsightsSender(instrumentationKey);

        collectMetrics(metricProvider, applicationInsightsSender);

        System.out.println("Exiting...");
    }

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
}