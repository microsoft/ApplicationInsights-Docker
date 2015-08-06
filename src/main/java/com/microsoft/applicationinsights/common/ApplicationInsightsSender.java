package com.microsoft.applicationinsights.common;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.contracts.ContainerStateEvent;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.internal.perfcounter.Constants;
import com.microsoft.applicationinsights.telemetry.MetricTelemetry;
import com.microsoft.applicationinsights.telemetry.PerformanceCounterTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import java.util.Map;

/**
 * Created by yonisha on 7/23/2015.
 */
public class ApplicationInsightsSender {
    private final static String INSTANCE_NAME_TOTAL = "_Total";
    private TelemetryClient telemetryClient;

    // region Ctor

    // Ctor for testability purposes.
    protected ApplicationInsightsSender(TelemetryClient telemetryClient) {
        this.telemetryClient = telemetryClient;
    }

    public ApplicationInsightsSender(String instrumentationKey) {
        TelemetryConfiguration telemetryConfiguration = TelemetryConfiguration.getActive();
        telemetryConfiguration.setInstrumentationKey(instrumentationKey);
        this.telemetryClient = new TelemetryClient(telemetryConfiguration);
    }

    // endregion Ctor

    // region Public

    public <T> void track(T metric) {
        Telemetry telemetry;

        if (metric instanceof ContainerStatsMetric) {
            telemetry = createTelemetry((ContainerStatsMetric)metric);
        } else if (metric instanceof ContainerStateEvent) {
            telemetry = createTelemetry((ContainerStateEvent)metric);
        } else {
            throw new IllegalArgumentException("Unknown metric: " + metric.getClass().getSimpleName());
        }

        this.telemetryClient.track(telemetry);
    }

    // endregion Public

    // region Private

    private Telemetry createTelemetry(ContainerStateEvent stateEvent) {
        return null;
    }

    private Telemetry createTelemetry(ContainerStatsMetric containerStatsMetric) {
        Telemetry telemetry;
        String metricName = containerStatsMetric.getMetricName();

        // If the given metric is one of the build-in PC in Ibiza, we track it as a performance counter telemetry.
        // Otherwise the given metric is sent as a custom metric.
        if (metricName.equalsIgnoreCase(Constants.CPU_PC_COUNTER_NAME) || metricName.equalsIgnoreCase(Constants.TOTAL_MEMORY_PC_COUNTER_NAME)) {
            telemetry = createPerformanceCounterTelemetry(containerStatsMetric);
        } else {
            MetricTelemetry metricTelemetry = new MetricTelemetry(metricName, containerStatsMetric.getValue());
            metricTelemetry.setMin(containerStatsMetric.getMin());
            metricTelemetry.setMax(containerStatsMetric.getMax());
            metricTelemetry.setCount(containerStatsMetric.getCount());
            metricTelemetry.setStandardDeviation(containerStatsMetric.getStdDev());

            telemetry = metricTelemetry;
        }

        Map<String, String> properties = telemetry.getProperties();
        properties.put("docker-host", containerStatsMetric.getDockerHost());
        properties.put("docker-image", containerStatsMetric.getDockerImage());
        properties.put("docker-container-name", containerStatsMetric.getDockerContainerName());
        properties.put("docker-container-id", containerStatsMetric.getDockerContainerId());

        return telemetry;
    }

    private PerformanceCounterTelemetry createPerformanceCounterTelemetry(ContainerStatsMetric containerStatsMetric) {
        PerformanceCounterTelemetry performanceCounterTelemetry = null;

        String metricName = containerStatsMetric.getMetricName();
        if (metricName.equalsIgnoreCase(Constants.CPU_PC_COUNTER_NAME)) {
            performanceCounterTelemetry = new PerformanceCounterTelemetry(
                    Constants.TOTAL_CPU_PC_CATEGORY_NAME,
                    Constants.CPU_PC_COUNTER_NAME,
                    INSTANCE_NAME_TOTAL,
                    containerStatsMetric.getValue());
        } else if (metricName.equalsIgnoreCase(Constants.TOTAL_MEMORY_PC_COUNTER_NAME)) {
            performanceCounterTelemetry = new PerformanceCounterTelemetry(
                    Constants.TOTAL_MEMORY_PC_CATEGORY_NAME,
                    Constants.TOTAL_MEMORY_PC_COUNTER_NAME,
                    "",
                    containerStatsMetric.getValue());
        }

        return performanceCounterTelemetry;
    }

    // endregion Private
}
