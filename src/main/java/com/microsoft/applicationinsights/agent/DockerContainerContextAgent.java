package com.microsoft.applicationinsights.agent;

import com.microsoft.applicationinsights.python.PythonBootstrapper;

import java.io.IOException;

/**
 * Created by yonisha on 7/28/2015.
 */
public class DockerContainerContextAgent implements Runnable {
    private PythonBootstrapper pythonBootstrapper;
    private boolean shouldStop = false;

    // region Ctor

    public DockerContainerContextAgent(PythonBootstrapper pythonBootstrapper) {
        this.pythonBootstrapper = pythonBootstrapper;
    }

    // endregion Ctor

    // region Public

    public void run() {

        // TODO: check python exit code and check if killed intentionally.
        while (!shouldStop) {
            try {
                this.pythonBootstrapper.start(true);
            } catch (IOException e) {}
        }
    }

    // endregion Public

    // region Private

    protected void stop() {
        this.shouldStop = true;
    }

    // endregion Private
}
