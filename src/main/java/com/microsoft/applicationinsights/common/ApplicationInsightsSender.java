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
