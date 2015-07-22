package com.microsoft.applicationinsights.contracts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by yonisha on 7/22/2015.
 */
public class ContainerStatsMetric {

    // region Members

    private String dockerHost;
    private String dockerImage;
    private String dockerContainerName;
    private String dockerContainerId;

    private String metricName;
    private double value;
    private int count;
    private double min;
    private double max;
    private double stdDev;

    // endregion Members

    // region Ctor

    public ContainerStatsMetric(String json) {
        deserialize(json);
    }

    // endregion Ctor

    // region Public Methods

    public String getDockerHost() {
        return dockerHost;
    }

    public String getDockerImage() {
        return dockerImage;
    }

    public String getDockerContainerName() {
        return dockerContainerName;
    }

    public String getDockerContainerId() {
        return dockerContainerId;
    }

    public String getMetricName() {
        return metricName;
    }

    public double getValue() {
        return value;
    }

    public int getCount() {
        return count;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStdDev() {
        return stdDev;
    }

    // endregion Public Methods

    // region Private Methods

    private void deserialize(String json) {
        try {
            JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
            JsonObject metricJson = jsonObj.getAsJsonObject("metric");

            this.metricName = metricJson.get("name").getAsString();
            this.value = metricJson.get("value").getAsDouble();
            this.count = metricJson.get("count").getAsInt();
            this.min = metricJson.get("min").getAsDouble();
            this.max = metricJson.get("max").getAsDouble();
            this.stdDev = metricJson.get("std").getAsDouble();

            JsonObject propertiesJson = jsonObj.getAsJsonObject("properties");
            this.dockerHost = propertiesJson.get("docker-host").getAsString();
            this.dockerImage = propertiesJson.get("docker-image").getAsString();
            this.dockerContainerName = propertiesJson.get("docker-container-name").getAsString();
            this.dockerContainerId = propertiesJson.get("docker-container-id").getAsString();
        } catch (Exception e) {
            System.out.println("Failed to parse Docker container stats metric with exception: " + e.getMessage());
        }
    }

    // endregion Private Methods
}