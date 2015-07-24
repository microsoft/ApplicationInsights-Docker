package com.microsoft.applicationinsights.agent;

import java.io.IOException;

/**
 * Created by yonisha on 7/22/2015.
 *
 * The agent is executed by the Docker ENTRYPOINT command.
 */
public class AgentBootstrapper {
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Instrumentation key required.");

            return;
        }

        System.out.println("Starting Application Insights Docker agent.");
        DockerAgent dockerAgent = new DockerAgent(args[0]);
        dockerAgent.run();

        System.out.println("Exiting...");
    }
}