package com.microsoft.applicationinsights.agent;

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

    private String defaultMetric = "{'metric':{'name':'name','value':0,'count':0,'min':0,'max':0,'std':0},'properties':{'docker-image':'x','docker-host':'x','docker-container-id':'x','docker-container-name':'x'}}";

    @Test
    public void testValidJsonResultsWithContainerStatsMetric() throws IOException {
        ContainerStatsMetric metric = createProviderAndGetMetric(defaultMetric);

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
