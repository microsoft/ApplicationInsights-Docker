package com.microsoft.applicationinsights.providers;

import com.microsoft.applicationinsights.common.TestConstants;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by yonisha on 7/23/2015.
 */
public class MetricProviderTests {

    @Test
    public void testValidJsonResultsWithContainerStatsMetric() throws IOException {
        ContainerStatsMetric metric = createProviderAndGetMetric(TestConstants.DEFAULT_METRIC_EVENT);

        Assert.assertNotNull(metric);
    }

    @Test
    public void testCorruptedJsonNotThrowException() throws IOException {
        ContainerStatsMetric metric = createProviderAndGetMetric(" { some corrupted json } ");

        Assert.assertNull(metric);
    }

    private ContainerStatsMetric createProviderAndGetMetric(String providerInputString) throws IOException {
        BufferedReader inputBuffer = new BufferedReader(new StringReader(providerInputString));

        MetricProvider metricProvider = new MetricProvider(inputBuffer);
        return metricProvider.getNext();
    }
}
