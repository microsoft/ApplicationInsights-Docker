package com.microsoft.applicationinsights.common;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.contracts.ContainerStateEvent;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.telemetry.Telemetry;

/**
 * Created by yonisha on 7/23/2015.
 */
public class ApplicationInsightsSender {
    private TelemetryClient telemetryClient;
    private TelemetryFactory telemetryFactory;

    // region Ctor

    // Ctor for testability purposes.
    protected ApplicationInsightsSender(TelemetryClient telemetryClient, TelemetryFactory telemetryFactory) {
        this.telemetryClient = telemetryClient;
        this.telemetryFactory = telemetryFactory;
    }

    public ApplicationInsightsSender(String instrumentationKey) {
        this(initializeTelemetryClient(instrumentationKey), new TelemetryFactory());
    }

    // endregion Ctor

    // region Public

    public <T> void track(T metric) {
        Telemetry telemetry;

        if (metric instanceof ContainerStatsMetric) {
            telemetry = this.telemetryFactory.createMetricTelemetry((ContainerStatsMetric) metric);
        } else if (metric instanceof ContainerStateEvent) {
            telemetry = this.telemetryFactory.createEventTelemetry((ContainerStateEvent) metric);
        } else {
            System.err.println("Unknown metric: " + metric.getClass().getSimpleName());

            return;
        }

        this.telemetryClient.track(telemetry);
    }

    // endregion Public

    // region Private

    private static TelemetryClient initializeTelemetryClient(String instrumentationKey) {
        TelemetryConfiguration telemetryConfiguration = TelemetryConfiguration.getActive();
        telemetryConfiguration.setInstrumentationKey(instrumentationKey);
        return new TelemetryClient(telemetryConfiguration);
    }

    // endregion Private
}
