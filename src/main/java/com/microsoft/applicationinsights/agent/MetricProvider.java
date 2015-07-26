package com.microsoft.applicationinsights.agent;

import com.google.gson.JsonSyntaxException;
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

    public ContainerStatsMetric getNext() {
        String metricsJson;

        try {
            metricsJson = inputBuffer.readLine();
        } catch (IOException e) {
            System.out.println("Failed to read metric from input stream with exception: " + e.getMessage());

            return null;
        }

        ContainerStatsMetric containerStatsMetric = null;
        if (metricsJson != null) {
            containerStatsMetric = createContainerStatsMetric(metricsJson);
        }

        return containerStatsMetric;
    }

    private ContainerStatsMetric createContainerStatsMetric(String json) {
        ContainerStatsMetric containerStatsMetric = null;

        try {
            containerStatsMetric = new ContainerStatsMetric(json);
        } catch (JsonSyntaxException e) {
            System.out.println("Failed to deserialize JSON to container metric: " + json);
        }

        return containerStatsMetric;
    }
}
