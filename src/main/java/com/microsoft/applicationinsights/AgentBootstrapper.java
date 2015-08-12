package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.common.ApplicationInsightsSender;
import com.microsoft.applicationinsights.agent.DockerAgent;
import com.microsoft.applicationinsights.agent.DockerContainerContextAgent;
import com.microsoft.applicationinsights.python.ContainerContextPythonBoostrapper;
import com.microsoft.applicationinsights.python.ContainerStatePythonBootstrapper;
import com.microsoft.applicationinsights.python.MetricCollectionPythonBoostrapper;
import com.microsoft.applicationinsights.python.PythonBootstrapper;

import java.io.IOException;

/**
 * Created by yonisha on 7/22/2015.
 *
 * The agent is executed by the Docker ENTRYPOINT command.
 */
public class AgentBootstrapper {

    // region Public

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Starting Application Insights Docker agent.");

        if (args.length == 0) {
            System.out.println("Instrumentation key required.");

            return;
        }

        String instrumentationKey = args[0];
        ApplicationInsightsSender applicationInsightsSender = new ApplicationInsightsSender(instrumentationKey);

        PythonBootstrapper metricCollectionBootstrapper = new MetricCollectionPythonBoostrapper();
        Thread metricCollectionAgentThread = createMetricCollectionProcess(applicationInsightsSender, metricCollectionBootstrapper);

        PythonBootstrapper containerStateBootstrapper = new ContainerStatePythonBootstrapper();
        Thread containerStateThread = createContainerStateProcess(applicationInsightsSender, containerStateBootstrapper);

        Thread containerContextAgentThread = createContainerContextProcess();

        AgentBootstrapper agentBootstrapper = new AgentBootstrapper();
        agentBootstrapper.run(applicationInsightsSender, metricCollectionAgentThread, containerStateThread, containerContextAgentThread);

        System.out.println("Shutting down Application Insights Docker agent.");
    }

    public void run(
            ApplicationInsightsSender applicationInsightsSender,
            Thread metricCollectionAgentThread,
            Thread containerStateThread,
            Thread containerContextAgentThread) throws InterruptedException {

        // Starting threads.
        metricCollectionAgentThread.start();
        containerContextAgentThread.start();
        containerStateThread.start();

        // Waiting for all threads.
        metricCollectionAgentThread.join();
        containerStateThread.join();
        containerContextAgentThread.join();
    }

    // endregion public

    // region Private

    protected static Thread createMetricCollectionProcess(ApplicationInsightsSender aiSender, PythonBootstrapper metricCollectionBootstrapper) {
        System.out.println("Starting metric collection process.");
        DockerAgent dockerMetricAgent = new DockerAgent(metricCollectionBootstrapper, aiSender);
        Thread metricCollectionAgentThread = new Thread(dockerMetricAgent);
        metricCollectionAgentThread.setDaemon(true);

        return metricCollectionAgentThread;
    }

    protected static Thread createContainerContextProcess() {
        System.out.println("Starting container context process.");
        PythonBootstrapper containerContextBootstrapper = new ContainerContextPythonBoostrapper();
        DockerContainerContextAgent containerContextAgent = new DockerContainerContextAgent(containerContextBootstrapper);
        Thread containerContextAgentThread = new Thread(containerContextAgent);
        containerContextAgentThread.setDaemon(true);

        return containerContextAgentThread;
    }

    protected static Thread createContainerStateProcess(ApplicationInsightsSender aiSender, PythonBootstrapper containerStateBootstrapper) {
        System.out.println("Starting container state process.");
        DockerAgent containerStateAgent = new DockerAgent(containerStateBootstrapper, aiSender);
        Thread containerStateThread = new Thread(containerStateAgent);
        containerStateThread.setDaemon(true);

        return containerStateThread;
    }

    // endregion Private
}