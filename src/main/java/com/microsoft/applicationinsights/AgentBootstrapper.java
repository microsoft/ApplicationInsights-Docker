package com.microsoft.applicationinsights;

import com.microsoft.applicationinsights.agent.ApplicationInsightsSender;
import com.microsoft.applicationinsights.agent.DockerAgent;
import com.microsoft.applicationinsights.python.PythonBootstrapper;

import java.io.IOException;

/**
 * Created by yonisha on 7/22/2015.
 *
 * The agent is executed by the Docker ENTRYPOINT command.
 */
public class AgentBootstrapper {
    private static final String PYTHON_BOOTSTRAP_SCRIPT = "python/bootstrap.py";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Instrumentation key required.");

            return;
        }

        String instrumentationKey = args[0];

        System.out.println("Starting Application Insights Docker agent.");
        ApplicationInsightsSender applicationInsightsSender = new ApplicationInsightsSender(instrumentationKey);
        PythonBootstrapper pythonBootstrapper = new PythonBootstrapper(PYTHON_BOOTSTRAP_SCRIPT);

        DockerAgent dockerAgent = new DockerAgent(pythonBootstrapper, applicationInsightsSender);
        dockerAgent.run();

        System.out.println("Exiting...");
    }
}