package com.microsoft.applicationinsights.common;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.contracts.ContainerStateEvent;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.extensibility.context.OperationContext;
import com.microsoft.applicationinsights.internal.perfcounter.Constants;
import com.microsoft.applicationinsights.telemetry.EventTelemetry;
import com.microsoft.applicationinsights.telemetry.MetricTelemetry;
import com.microsoft.applicationinsights.telemetry.PerformanceCounterTelemetry;
import com.microsoft.applicationinsights.telemetry.Telemetry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

/**
 * Created by yonisha on 7/23/2015.
 */
public class ApplicationInsightsSenderTests {
    private final String METRIC_TEMPLATE = "{'metric':{'name':'%s','value':0,'count':0,'min':0,'max':0,'std':0},'properties':{'Docker image':'x','Docker host':'x','Docker container id':'x','Docker container name':'x'}}";

    private TelemetryClient telemetryClientMock;
    private ApplicationInsightsSender defaultSenderUnderTest;
    private List<Telemetry> telemetries = new ArrayList<Telemetry>();

    @Before
    public void testInitialize() {
        initializeTelemetryClientMock();
        defaultSenderUnderTest = new ApplicationInsightsSender(telemetryClientMock, new TelemetryFactory());
        telemetries = new ArrayList<Telemetry>();
    }

    @Test
    public void testCustomMetricClassifiedCorrectly() {
        testMetricClassifiedCorrectly(false, MetricTelemetry.class);
    }

    @Test
    public void testPerformanceCounterClassifiedCorrectly() {
        testMetricClassifiedCorrectly(true, PerformanceCounterTelemetry.class);
    }

    @Test
    public void testContaienrStateMetricClassifiedCorrectly() {
        trackContainerStateMetric();

        Mockito.verify(telemetryClientMock, times(1)).track(any(Telemetry.class));
        Assert.assertTrue(telemetries.get(0) instanceof EventTelemetry);
    }

    @Test
    public void testContainerStateTelemetryEventAssignedWithCorrelationId() {
        trackContainerStateMetric();

        OperationContext operation = telemetries.get(0).getContext().getOperation();
        Assert.assertEquals("con_id" , operation.getId());
        Assert.assertEquals("docker-container-state" , operation.getName());
    }

    private void trackContainerStateMetric() {
        ContainerStateEvent containerStateEvent = new ContainerStateEvent(TestConstants.DEFAULT_STATE_EVENT);
        defaultSenderUnderTest.track(containerStateEvent);
    }

    private void testMetricClassifiedCorrectly(boolean generatePerformanceCounterMetricName, Class expectedTelemetryType) {
        ContainerStatsMetric containerStatsMetric = createContainerStatsMetric(generatePerformanceCounterMetricName);

        defaultSenderUnderTest.track(containerStatsMetric);

        Mockito.verify(telemetryClientMock, times(1)).track(any(Telemetry.class));
        Assert.assertTrue(expectedTelemetryType.isInstance(telemetries.get(0)));
    }

    private ContainerStatsMetric createContainerStatsMetric(boolean isPerformanceCounter) {
        String metricName = isPerformanceCounter ? Constants.CPU_PC_COUNTER_NAME : "non_pc";
        String metricJson = String.format(METRIC_TEMPLATE, metricName);

        return new ContainerStatsMetric(metricJson);
    }

    private void initializeTelemetryClientMock() {
        telemetryClientMock = mock(com.microsoft.applicationinsights.TelemetryClient.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Telemetry telemetry = ((Telemetry) invocation.getArguments()[0]);
                telemetries.add(telemetry);

                return null;
            }
        }).when(telemetryClientMock).track(Matchers.any(Telemetry.class));
    }
}
