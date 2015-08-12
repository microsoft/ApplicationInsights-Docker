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
