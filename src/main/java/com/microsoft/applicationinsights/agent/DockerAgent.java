package com.microsoft.applicationinsights.agent;

import com.microsoft.applicationinsights.common.ApplicationInsightsSender;
import com.microsoft.applicationinsights.providers.EventProvider;
import com.microsoft.applicationinsights.python.PythonBootstrapper;

import java.io.IOException;

/**
 * Created by yonisha on 8/5/2015.
 */
public class DockerAgent<T> implements Runnable {
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
     * This method starts a Python process using the provided bootstrapper.
     * If, for any reason, the Python process exits, we start another process and continue to collect events.
     */
    public void run() {

        // TODO: check python exit code and check if killed intentionally.
        while (!shouldStop && this.pythonBootstrapper.getExitValue() != 0) {

            try {
                this.pythonBootstrapper.start(false);
            } catch (IOException e) {}

            EventProvider eventProvider = (EventProvider)this.pythonBootstrapper.getResult();
            if (eventProvider != null) {
                collectAndSendEvents(eventProvider, this.applicationInsightsSender);
            }

            String processExitInfo = this.pythonBootstrapper.getProcessExitInfo();
            System.out.println(processExitInfo);
        }
    }

    // endregion Public

    // region Private

    protected void stop() {
        this.shouldStop = true;
    }

    private void collectAndSendEvents(EventProvider<T> eventProvider, ApplicationInsightsSender applicationInsightsSender) {
        System.out.println("Starting to collect events from: " + this.pythonBootstrapper.getClass().getSimpleName());

        while (true) {
            T event = eventProvider.getNext();

            // Event can be null in two cases:
            // 1) The underlying JSON is corrupted and cannot be serialized. In that case, make sure only event JSONs
            // strings are printed to the STDOUT in the python scripts. Any other user traces are not allowed.
            // 2) The Python process has exited. In that case, the agent will start another Python process.
            if (event == null) {
                if (!this.pythonBootstrapper.isAlive()) {
                    break;
                } else {
                    continue;
                }
            }

            applicationInsightsSender.track(event);
        }
    }

    // endregion Private
}
