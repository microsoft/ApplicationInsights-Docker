package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.common.ApplicationInsightsSender;
import com.microsoft.applicationinsights.agent.DockerAgent;
import com.microsoft.applicationinsights.agent.DockerContainerContextAgent;
import com.microsoft.applicationinsights.python.ContainerContextPythonBoostrapper;
import com.microsoft.applicationinsights.python.ContainerStatePythonBootstrapper;
import com.microsoft.applicationinsights.python.MetricCollectionPythonBoostrapper;
import com.microsoft.applicationinsights.python.PythonBootstrapper;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by yonisha on 7/22/2015.
 *
 * The agent is executed by the Docker ENTRYPOINT command.
 */
public class AgentBootstrapper {

    // region Consts

    private static final int DEFAULT_COLLECT_INTERVAL = 45;
    private static final String CONTAINER_USAGE_COMMAND =
            "docker run -v /var/run/docker.sock:/docker.sock -d ai-develop ikey=<Instrumentation_Key>";

    // endregion Consts

    // region Public

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Starting Application Insights Docker agent.");
        HashMap<String, String> argumentsMap = parseArguments(args);

        String instrumentationKey = argumentsMap.get("ikey");
        if (instrumentationKey == null) {
            System.out.println("Usage: " + CONTAINER_USAGE_COMMAND);

            return;
        }

        // TODO: Create object for argument verification (type, existence, default value etc.)
        ApplicationInsightsSender applicationInsightsSender = new ApplicationInsightsSender(instrumentationKey);

        int sampleRateFromArgument = getSampleRateFromArgument(argumentsMap);
        PythonBootstrapper metricCollectionBootstrapper = new MetricCollectionPythonBoostrapper(sampleRateFromArgument);
        Thread metricCollectionAgentThread = createMetricCollectionProcess(applicationInsightsSender, metricCollectionBootstrapper);

        PythonBootstrapper containerStateBootstrapper = new ContainerStatePythonBootstrapper();
        Thread containerStateThread = createContainerStateProcess(applicationInsightsSender, containerStateBootstrapper);

        Thread containerContextAgentThread = createContainerContextProcess();

        AgentBootstrapper agentBootstrapper = new AgentBootstrapper();
        agentBootstrapper.run(applicationInsightsSender, metricCollectionAgentThread, containerStateThread, containerContextAgentThread);

        System.out.println("Shutting down Application Insights Docker agent.");
    }

    // TODO: move to a dedicate object
    private static int getSampleRateFromArgument(HashMap<String, String> argumentsMap) {
        final String intervalArgument = "collect-interval";
        String sampleIntervalStr = argumentsMap.get(intervalArgument);

        int sampleInterval = DEFAULT_COLLECT_INTERVAL;
        if (sampleIntervalStr != null) {
            try {
                sampleInterval = (int)Double.parseDouble(sampleIntervalStr);
            } catch (NumberFormatException e) {
                System.err.print("Failed to parse '" + sampleIntervalStr +  "' as '" + intervalArgument + "' argument - must be a number.");
            }
        } else {
            System.out.println("No collect interval argument provided.");
        }

        System.out.println("Collect interval is set to " + sampleInterval + " seconds.");

        return sampleInterval;
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

    private static HashMap parseArguments(String[] args) {
        HashMap<String, String> arguments = new HashMap<String, String>();

        for (String arg : args) {
            String[] kv = arg.split("=");
            if (kv.length == 2) {
                arguments.put(kv[0], kv[1]);
            }
        }

        return arguments;
    }

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