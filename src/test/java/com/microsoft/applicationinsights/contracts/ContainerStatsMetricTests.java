package com.microsoft.applicationinsights.contracts;

import com.microsoft.applicationinsights.common.Constants;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by yonisha on 7/22/2015.
 */
public class ContainerStatsMetricTests {
    private final String METRIC_NAME = "docker-memory-usage";
    private final String DOCKER_HOST = "ubuntu-vm";
    private final String DOCKER_IMAGE = "hybrid";
    private final String DOCKER_CONTAINER_NAME = "carlos";
    private final String DOCKER_CONTAINER_ID = "some_id";

    private final float VALUE = 0.01f;
    private final float MIN = 0.02f;
    private final float MAX = 0.03f;
    private final float STD_DEV = 0.04f;
    private final int COUNT = 5;

    private String defaultMetric = String.format(Constants.DEFAULT_METRIC_TEMPLATE, METRIC_NAME, VALUE, COUNT, MIN, MAX, STD_DEV, DOCKER_IMAGE, DOCKER_HOST, DOCKER_CONTAINER_ID, DOCKER_CONTAINER_NAME);

    @Test
    public void testMetricJsonDeserializedSuccessfully() {
        ContainerStatsMetric containerStatsMetric = new ContainerStatsMetric(defaultMetric);

        Assert.assertEquals(METRIC_NAME, containerStatsMetric.getMetricName());
        Assert.assertEquals(DOCKER_HOST, containerStatsMetric.getDockerHost());
        Assert.assertEquals(DOCKER_IMAGE, containerStatsMetric.getDockerImage());
        Assert.assertEquals(DOCKER_CONTAINER_NAME, containerStatsMetric.getDockerContainerName());
        Assert.assertEquals(DOCKER_CONTAINER_ID, containerStatsMetric.getDockerContainerId());
        Assert.assertEquals(VALUE, containerStatsMetric.getValue(), 0.001);
        Assert.assertEquals(MIN, containerStatsMetric.getMin(), 0.001);
        Assert.assertEquals(MAX, containerStatsMetric.getMax(), 0.001);
        Assert.assertEquals(STD_DEV, containerStatsMetric.getStdDev(), 0.001);
        Assert.assertEquals(COUNT, containerStatsMetric.getCount());
    }
}
