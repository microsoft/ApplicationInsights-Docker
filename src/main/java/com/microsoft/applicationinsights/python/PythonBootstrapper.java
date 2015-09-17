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

package com.microsoft.applicationinsights.python;

import java.io.IOException;
import com.microsoft.applicationinsights.common.ArrayUtils;

/**
 * Created by yonisha on 7/22/2015.
 */
public abstract class PythonBootstrapper<T> {

    // region Members

    private static final String PYTHON_BOOTSTRAP_SCRIPT = "python/bootstrap.py";
    protected ProcessBuilder processBuilder;
    protected Process process;

    // endregion Members

    // region Ctor

    protected PythonBootstrapper(ProcessBuilder processBuilder) {
        this.processBuilder = processBuilder;
    }

    public PythonBootstrapper(String... bootstrapperArgs) {
        String[] updatedParams = ArrayUtils.addFirst(PYTHON_BOOTSTRAP_SCRIPT, bootstrapperArgs);

        this.processBuilder = new PythonProcessBuilder(updatedParams);
    }

    // endregion Ctor

    // region Public

    public void start(boolean waitForExit) throws IOException {
        closePreviousProcessResources();

        try {
            this.process = processBuilder.start();
            if (waitForExit) {
                this.process.waitFor();
            }
        } catch (IOException e) {
            System.out.println(this.getClass().getSimpleName() + " failed to start python process with error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(this.getClass().getSimpleName() + " has been interrupted. Error: " + e.getMessage());
        }
    }

    public boolean isAlive() {
        if (process == null) {
            return false;
        }

        return this.getExitValue() == -1;
    }

    public abstract <T> T getResult();

    public int getExitValue() {
        if (this.process == null) {
            return -1;
        }

        try {
            return this.process.exitValue();
        } catch (java.lang.IllegalThreadStateException e) {
            return -1;
        }
    }

    public String getProcessExitInfo() {
        String bootstrapperClassName = this.getClass().getSimpleName();

        return "Python bootstrapper " +  bootstrapperClassName + " has exited with exit code: " + this.getExitValue();
    }

    // endregion Public

    // region Private

    protected void closePreviousProcessResources() {
        if (process != null) {
            try {
                if (this.process.getInputStream() != null) {
                    process.getInputStream().close();
                }
                if (this.process.getErrorStream() != null) {
                    process.getErrorStream().close();
                }
            } catch (IOException e) {
            }
        }
    }

    // endregion Private
}
