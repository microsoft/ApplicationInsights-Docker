package com.microsoft.applicationinsights.agent;

import java.io.IOException;

/**
 * Created by yonisha on 7/22/2015.
 *
 * The agent is executed by the Docker CMD command.
 */
public class DockerAgent {
    private static final String PYTHON_BOOTSTRAP_SCRIPT = "python/bootstrap.py";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Instrumentation key required.");

            return;
        }

        String instrumentationKey = args[0];
        PythonBootstrapper pythonBootstrapper = new PythonBootstrapper(PYTHON_BOOTSTRAP_SCRIPT, instrumentationKey);

        System.out.println("Starting Python bootsrapper");
        pythonBootstrapper.start();

        System.out.println("Instrumentation key required.");
    }
}