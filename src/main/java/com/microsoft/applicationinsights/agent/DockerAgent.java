package com.microsoft.applicationinsights.agent;

import java.io.IOException;

/**
 * Created by yonisha on 7/22/2015.
 */
public class DockerAgent {
    private static final String PYTHON_BOOTSTRAP_SCRIPT = "bootstrap.py";

    public static void main(String[] args) throws IOException {
        if (false && args.length == 0) {
            System.out.println("Instrumentation key required.");

            return;
        }

        PythonBootstraper pythonBootstraper = new PythonBootstraper(PYTHON_BOOTSTRAP_SCRIPT);
        pythonBootstraper.start();
    }
}