package com.microsoft.applicationinsights.python;

import java.io.IOException;

/**
 * Created by yonisha on 7/26/2015.
 */
public class PythonProcessBuilder  implements ProcessBuilder {

    private final java.lang.ProcessBuilder processBuilder;
    private final String PYTHON_EXE_NAME = "python";

    public PythonProcessBuilder(String pythonScriptName, String bootstrapperArg) {
        this.processBuilder = new java.lang.ProcessBuilder(PYTHON_EXE_NAME, pythonScriptName, bootstrapperArg);
    }

    public Process start() throws IOException {
        return processBuilder.start();
    }
}
