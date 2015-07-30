package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.agent.ApplicationInsightsSender;
import com.microsoft.applicationinsights.agent.DockerContainerContextAgent;
import com.microsoft.applicationinsights.agent.DockerMetricAgent;
import com.microsoft.applicationinsights.python.ContainerContextPythonBoostrapper;
import com.microsoft.applicationinsights.python.MetricCollectionPythonBoostrapper;
import com.microsoft.applicationinsights.python.PythonBootstrapper;

import java.io.IOException;

/**
 * Created by yonisha on 7/22/2015.
 *
 * The agent is executed by the Docker ENTRYPOINT command.
 */
public class AgentBootstrapper {

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Starting Application Insights Docker agent.");

        if (args.length == 0) {
            System.out.println("Instrumentation key required.");

            return;
        }

        System.out.println("Starting metric collection process.");
        String instrumentationKey = args[0];
        ApplicationInsightsSender applicationInsightsSender = new ApplicationInsightsSender(instrumentationKey);
        PythonBootstrapper metricCollectionBootstrapper = new MetricCollectionPythonBoostrapper();
        DockerMetricAgent dockerMetricAgent = new DockerMetricAgent(metricCollectionBootstrapper, applicationInsightsSender);
        Thread metricCollectionAgentThread = new Thread(dockerMetricAgent);
        metricCollectionAgentThread.start();

        System.out.println("Starting container context process.");
        PythonBootstrapper containerContextBootstrapper = new ContainerContextPythonBoostrapper();
        DockerContainerContextAgent containerContextAgent = new DockerContainerContextAgent(containerContextBootstrapper);
        Thread containerContextAgentThread = new Thread(containerContextAgent);
        containerContextAgentThread.start();

        // Waiting for all threads.
        metricCollectionAgentThread.join();
        containerContextAgentThread.join();

        System.out.println("Shutting down Application Insights Docker agent.");
    }
}