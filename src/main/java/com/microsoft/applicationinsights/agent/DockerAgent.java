package com.microsoft.applicationinsights.agent;

import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;
import com.microsoft.applicationinsights.python.PythonBootstrapper;

import java.io.IOException;

/**
 * Created by yonisha on 7/23/2015.
 */
public class DockerAgent {
    private ApplicationInsightsSender applicationInsightsSender;
    private PythonBootstrapper pythonBootstrapper;
    private boolean shouldStop = false;

    // region Ctor

    public DockerAgent(PythonBootstrapper pythonBootstrapper, ApplicationInsightsSender applicationInsightsSender) {
        this.applicationInsightsSender = applicationInsightsSender;
        this.pythonBootstrapper = pythonBootstrapper;
    }

    // endregion Ctor

    // region Public

    /**
     * This method starts a Python process to collect Docker metrics.
     * If, for any reason, the Python process exits, we start another process and continue to collect metrics.
     * @throws IOException
     */
    public void run() throws IOException {

        // TODO: check python exit code and check if killed intentionally.
        while (true && !shouldStop) {
            MetricProvider metricProvider = this.pythonBootstrapper.start();
            collectAndSendMetrics(metricProvider, this.applicationInsightsSender);
        }
    }

    // endregion Public

    // region Private

    protected void stop() {
        this.shouldStop = true;
    }

    private void collectAndSendMetrics(MetricProvider metricProvider, ApplicationInsightsSender applicationInsightsSender) {
        System.out.println("Starting to collect metrics.");

        while (true) {
            ContainerStatsMetric metric = metricProvider.getNext();

            // Metric can be null in two cases:
            // 1) The underlying JSON is corrupted and cannot be serialized. In that case, make sure only metric JSONs
            // strings are printed to the STDOUT in the python scripts. Any other user traces are not allowed.
            // 2) The Python process has exited. In that case, the agent will start another Python process.
            if (metric == null) {
                if (!this.pythonBootstrapper.isAlive()) {
                    break;
                } else {
                    continue;
                }
            }

            applicationInsightsSender.sentMetric(metric);
        }
    }

    // endregion Private
}
