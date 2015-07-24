package com.microsoft.applicationinsights.agent;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.TelemetryConfiguration;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.telemetry.MetricTelemetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * Created by yonisha on 7/22/2015.
 */
public class PythonBootstrapper {

    // region Members

    private final String PYTHON_EXE_NAME = "python";
    private String pythonScriptName;

    // endregion Members

    // region Ctor

    public PythonBootstrapper(String pythonScriptName) {
        this.pythonScriptName = pythonScriptName;
    }

    // endregion Ctor

    // region Public

    public MetricProvider start() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(PYTHON_EXE_NAME, this.pythonScriptName);
        Process process;

        try {
            process = pb.start();
        } catch (IOException e) {
            System.out.println(this.getClass().getSimpleName() + " failed to start python process with error: " + e.getMessage());

            throw e;
        }

        return new MetricProvider(new BufferedReader(new InputStreamReader(process.getInputStream())));
    }

    // endregion Public
}
