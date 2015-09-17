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

import com.microsoft.applicationinsights.contracts.ContainerStateEvent;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.telemetry.EventTelemetry;
import com.microsoft.applicationinsights.telemetry.MetricTelemetry;
import com.microsoft.applicationinsights.telemetry.PerformanceCounterTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by yonisha on 8/12/2015.
 */
public class TelemetryFactoryTests {

    private static final String METRIC_TEMPLATE = "{'metric':{'name':'%s','value':0,'count':0,'min':0,'max':0,'std':0},'properties':{'Docker image':'%s','Docker host':'%s','Docker container id':'%s','Docker container name':'%s'}}";
    private TelemetryFactory telemetryFactoryUnderTest = new TelemetryFactory();

    @Test
    public void testCreateEventTelemetry() {
        ContainerStateEvent containerStateEvent = new ContainerStateEvent(TestConstants.DEFAULT_STATE_EVENT);

        EventTelemetry eventTelemetry = (EventTelemetry)this.telemetryFactoryUnderTest.createEventTelemetry(containerStateEvent);

        Assert.assertEquals("docker-container-state", eventTelemetry.getName());
        Assert.assertEquals("con_id", eventTelemetry.getProperties().get(Constants.DOCKER_CONTAINER_ID_PROPERTY_KEY));
    }

    @Test
    public void testCreateCustomMetricTelemetry() {
        ContainerStatsMetric customMetric = new ContainerStatsMetric(TestConstants.DEFAULT_METRIC_EVENT);

        MetricTelemetry metricTelemetry = (MetricTelemetry)this.telemetryFactoryUnderTest.createMetricTelemetry(customMetric);

        Assert.assertEquals("name", metricTelemetry.getName());
    }

    @Test
    public void testProcessorCpuStatsGeneratesCorrectPerformanceCounterTelemetry() {
        String cpuPcCounterName = com.microsoft.applicationinsights.internal.perfcounter.Constants.CPU_PC_COUNTER_NAME;

        PerformanceCounterTelemetry telemetry = (PerformanceCounterTelemetry) createPerformanceCounterTelemetryAccordingToMetricName(cpuPcCounterName);

        Assert.assertEquals(com.microsoft.applicationinsights.internal.perfcounter.Constants.CPU_PC_COUNTER_NAME, telemetry.getCounterName());
        Assert.assertEquals(com.microsoft.applicationinsights.internal.perfcounter.Constants.TOTAL_CPU_PC_CATEGORY_NAME, telemetry.getCategoryName());
        Assert.assertEquals(com.microsoft.applicationinsights.internal.perfcounter.Constants.INSTANCE_NAME_TOTAL, telemetry.getInstanceName());
    }

    @Test
    public void testAvailableMemoryStatsGeneratesCorrectPerformanceCounterTelemetry() {
        String availableMemoryCounterName = com.microsoft.applicationinsights.internal.perfcounter.Constants.TOTAL_MEMORY_PC_COUNTER_NAME;

        PerformanceCounterTelemetry telemetry = (PerformanceCounterTelemetry) createPerformanceCounterTelemetryAccordingToMetricName(availableMemoryCounterName);

        Assert.assertEquals(com.microsoft.applicationinsights.internal.perfcounter.Constants.TOTAL_MEMORY_PC_CATEGORY_NAME, telemetry.getCategoryName());
        Assert.assertEquals(com.microsoft.applicationinsights.internal.perfcounter.Constants.TOTAL_MEMORY_PC_COUNTER_NAME, telemetry.getCounterName());
    }

    @Test
    public void testEventTelemetryUpdatedWithInstrumentationKeyIfProvided() {
        ContainerStateEvent containerStateEvent = new ContainerStateEvent(TestConstants.DEFAULT_STATE_EVENT);

        EventTelemetry eventTelemetry = (EventTelemetry)this.telemetryFactoryUnderTest.createEventTelemetry(containerStateEvent);

        Assert.assertEquals("instrumentation_key", eventTelemetry.getContext().getInstrumentationKey());
    }

    @Test
    public void testEventTelemetryNotUpdatedWithInstrumentationKeyIfNotProvided() {
        String stateEventJsonWithoutIkey = TestConstants.DEFAULT_STATE_EVENT.replace("instrumentation_key", "");
        ContainerStateEvent containerStateEvent = new ContainerStateEvent(stateEventJsonWithoutIkey);

        EventTelemetry eventTelemetry = (EventTelemetry)this.telemetryFactoryUnderTest.createEventTelemetry(containerStateEvent);

        Assert.assertEquals(null, eventTelemetry.getContext().getInstrumentationKey());
    }

    private Telemetry createPerformanceCounterTelemetryAccordingToMetricName(String counterName) {

        String metric = String.format(METRIC_TEMPLATE, counterName, "some_host", "some_image", "some_con_name", "some_con_id");
        ContainerStatsMetric customMetric = new ContainerStatsMetric(metric);

        PerformanceCounterTelemetry pcTelemetry = (PerformanceCounterTelemetry)this.telemetryFactoryUnderTest.createMetricTelemetry(customMetric);

        return pcTelemetry;
    }
}
