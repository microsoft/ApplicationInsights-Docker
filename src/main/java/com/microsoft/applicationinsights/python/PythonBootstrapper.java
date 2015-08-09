package com.microsoft.applicationinsights.python;

import com.microsoft.applicationinsights.common.ArrayUtils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

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

            throw e;
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
        String exitInfo;
        String bootstrapperClassName = this.getClass().getSimpleName();
        exitInfo = "Python bootstrapper " +  bootstrapperClassName + " has exited with exit code: " + this.getExitValue() + "\n";

        if (this.getExitValue() != 0) {
            exitInfo += "Error message: " + this.getErrorOutput();
        }

        return exitInfo;
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

    private String getErrorOutput() {
        if (this.process == null) {
            return null;
        }

        InputStream errorStream = this.process.getErrorStream();
        String errorMessage = null;
        try {
            errorMessage = IOUtils.toString(errorStream, "utf-8");
        } catch (IOException e) {
            System.out.println("Failed to get error message for python process: " + this.getClass().getSimpleName());
        }

        return errorMessage;
    }

    // endregion Private
}
