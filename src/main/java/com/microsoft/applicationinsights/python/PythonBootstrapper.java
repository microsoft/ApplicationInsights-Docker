package com.microsoft.applicationinsights.python;

import com.microsoft.applicationinsights.agent.MetricProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by yonisha on 7/22/2015.
 */
public class PythonBootstrapper {

    // region Members

    ProcessBuilder processBuilder;
    private Process process;

    // endregion Members

    // region Ctor

    protected PythonBootstrapper(ProcessBuilder processBuilder) {
        this.processBuilder = processBuilder;
    }

    public PythonBootstrapper(String pythonScriptName) {
        this.processBuilder = new PythonProcessBuilder(pythonScriptName);
    }

    // endregion Ctor

    // region Public

    public MetricProvider start() throws IOException {
        System.out.println("Starting Python bootsrapper.");

        closePreviousProcessResources();

        try {
            this.process = processBuilder.start();
        } catch (IOException e) {
            System.out.println(this.getClass().getSimpleName() + " failed to start python process with error: " + e.getMessage());

            throw e;
        }

        System.out.println("Python process is running.");

        return new MetricProvider(new BufferedReader(new InputStreamReader(process.getInputStream())));
    }

    public boolean isAlive() {
        if (process == null) {
            return false;
        }

        try {
            process.exitValue();

            return false;
        } catch (java.lang.IllegalThreadStateException e) {
            return true;
        }
    }

    // endregion Public

    // region Private

    protected void closePreviousProcessResources() {
        if (process != null) {
            try {
                process.getInputStream().close();
            } catch (IOException e) {
            }
        }
    }

    // endregion Private
}
