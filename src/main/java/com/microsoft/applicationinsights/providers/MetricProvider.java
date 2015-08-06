package com.microsoft.applicationinsights.providers;

import com.google.gson.JsonSyntaxException;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;

import java.io.BufferedReader;

/**
 * Created by yonisha on 7/23/2015.
 */
public class MetricProvider extends EventProvider<ContainerStatsMetric> {

    public MetricProvider(BufferedReader inputBuffer) {
        super(inputBuffer);
    }

    protected ContainerStatsMetric deserialize(String json) {
        ContainerStatsMetric containerStatsMetric = null;

        try {
            containerStatsMetric = new ContainerStatsMetric(json);
        } catch (JsonSyntaxException e) {
            System.out.println("Failed to deserialize JSON to container metric: " + json);
        }

        return containerStatsMetric;
    }
}
