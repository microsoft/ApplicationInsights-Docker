package com.microsoft.applicationinsights.agent;

import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by yonisha on 7/23/2015.
 */
public class MetricProvider {

    private BufferedReader inputBuffer;

    public MetricProvider(BufferedReader inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public ContainerStatsMetric getNext() throws IOException {
        String metricsJson = inputBuffer.readLine();

        ContainerStatsMetric containerStatsMetric = null;
        if (metricsJson != null) {
            containerStatsMetric = new ContainerStatsMetric(metricsJson);
        }

        return containerStatsMetric;
    }
}
