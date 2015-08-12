package com.microsoft.applicationinsights.common;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.contracts.ContainerStateEvent;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.internal.perfcounter.Constants;
import com.microsoft.applicationinsights.telemetry.EventTelemetry;
import com.microsoft.applicationinsights.telemetry.MetricTelemetry;
import com.microsoft.applicationinsights.telemetry.PerformanceCounterTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import java.util.Map;

/**
 * Created by yonisha on 7/23/2015.
 */
public class ApplicationInsightsSender {
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
            telemetry = createEventTelemetry((ContainerStatsMetric) metric);
        } else if (metric instanceof ContainerStateEvent) {
            telemetry = createEventTelemetry((ContainerStateEvent) metric);
        } else {
            System.err.println("Unknown metric: " + metric.getClass().getSimpleName());

            return;
        }

        this.telemetryClient.track(telemetry);
    }

    // endregion Public

    // region Private

    private Telemetry createEventTelemetry(ContainerStateEvent stateEvent) {
        EventTelemetry telemetry = new EventTelemetry(stateEvent.getName());

        // Setting operation in order to be able to correlate events related to the same container.
        String containerId = stateEvent.getProperties().get(com.microsoft.applicationinsights.common.Constants.DOCKER_CONTAINER_ID_PROPERTY_KEY);
        telemetry.getContext().getOperation().setId(containerId);
        telemetry.getContext().getOperation().setName(stateEvent.getName());

        telemetry.getProperties().putAll(stateEvent.getProperties());

        return telemetry;
    }

    private Telemetry createEventTelemetry(ContainerStatsMetric containerStatsMetric) {
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
        properties.put(com.microsoft.applicationinsights.common.Constants.DOCKER_HOST_PROPERTY_KEY, containerStatsMetric.getDockerHost());
        properties.put(com.microsoft.applicationinsights.common.Constants.DOCKER_IMAGE_PROPERTY_KEY, containerStatsMetric.getDockerImage());
        properties.put(com.microsoft.applicationinsights.common.Constants.DOCKER_CONTAINER_NAME_PROPERTY_KEY, containerStatsMetric.getDockerContainerName());
        properties.put(com.microsoft.applicationinsights.common.Constants.DOCKER_CONTAINER_ID_PROPERTY_KEY, containerStatsMetric.getDockerContainerId());

        return telemetry;
    }

    private PerformanceCounterTelemetry createPerformanceCounterTelemetry(ContainerStatsMetric containerStatsMetric) {
        PerformanceCounterTelemetry performanceCounterTelemetry = null;

        String metricName = containerStatsMetric.getMetricName();
        if (metricName.equalsIgnoreCase(Constants.CPU_PC_COUNTER_NAME)) {
            performanceCounterTelemetry = new PerformanceCounterTelemetry(
                    Constants.TOTAL_CPU_PC_CATEGORY_NAME,
                    Constants.CPU_PC_COUNTER_NAME,
                    Constants.INSTANCE_NAME_TOTAL,
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
