package com.microsoft.applicationinsights.agent;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.telemetry.MetricTelemetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by yonisha on 7/22/2015.
 */
public class PythonBootstrapper {

    // region Members

    private String pythonScriptName;
    private TelemetryClient telemetryClient;

    // endregion Members

    // region Ctor

    public PythonBootstrapper(String pythonScriptName, String instrumentationKey) {
        this.pythonScriptName = pythonScriptName;

        TelemetryConfiguration telemetryConfiguration = TelemetryConfiguration.getActive();
        telemetryConfiguration.setInstrumentationKey(instrumentationKey);
        this.telemetryClient = new TelemetryClient(telemetryConfiguration);
    }

    // endregion Ctor

    // region Public

    public void start() {
        ProcessBuilder pb = new ProcessBuilder("python", this.pythonScriptName);

        try {
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));

            String metricsJson = in.readLine();
            ContainerStatsMetric containerStatsMetric = new ContainerStatsMetric(metricsJson);
            MetricTelemetry metricTelemetry = createMetricTelemetry(containerStatsMetric);
            telemetryClient.trackMetric(metricTelemetry);
        } catch (IOException e) {
            System.out.println(this.getClass().getSimpleName() + " failed with error: " + e.getMessage());
        }
    }

    // endregion Public

    // region Private

    private MetricTelemetry createMetricTelemetry(ContainerStatsMetric containerStatsMetric) {
        MetricTelemetry metricTelemetry = new MetricTelemetry(containerStatsMetric.getMetricName(), containerStatsMetric.getValue());
        metricTelemetry.setMin(containerStatsMetric.getMin());
        metricTelemetry.setMax(containerStatsMetric.getMax());
        metricTelemetry.setCount(containerStatsMetric.getCount());
        metricTelemetry.setStandardDeviation(containerStatsMetric.getStdDev());

        Map<String, String> properties = metricTelemetry.getProperties();
        properties.put("docker-host", containerStatsMetric.getDockerHost());
        properties.put("docker-image", containerStatsMetric.getDockerImage());
        properties.put("docker-container-name", containerStatsMetric.getDockerContainerName());
        properties.put("docker-container-id", containerStatsMetric.getDockerContainerId());

        return metricTelemetry;
    }

    // endregion Private
}
