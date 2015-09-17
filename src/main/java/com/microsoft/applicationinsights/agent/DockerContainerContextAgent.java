/*
 * ApplicationInsights-Docker
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
        while (!shouldStop && this.pythonBootstrapper.getExitValue() != 0) {
            try {
                this.pythonBootstrapper.start(true);
            } catch (IOException e) {
                String simpleName = this.pythonBootstrapper.getClass().getSimpleName();
                System.out.println(simpleName + " failed with exception: " + e.getMessage());

                String processExitInfo = this.pythonBootstrapper.getProcessExitInfo();
                System.out.println(processExitInfo);
            }
        }
    }

    // endregion Public

    // region Private

    protected void stop() {
        this.shouldStop = true;
    }

    // endregion Private
}
