package com.microsoft.applicationinsights.python;

import com.microsoft.applicationinsights.common.ArrayUtils;

import java.io.IOException;

/**
 * Created by yonisha on 7/26/2015.
 */
public class PythonProcessBuilder  implements ProcessBuilder {

    private final java.lang.ProcessBuilder processBuilder;
    private final String PYTHON_EXE_NAME = "python";

    public PythonProcessBuilder(String... builderParams) {
        String[] updatedParams = ArrayUtils.addFirst(PYTHON_EXE_NAME, builderParams);

        this.processBuilder = new java.lang.ProcessBuilder(updatedParams);
    }

    public Process start() throws IOException {
        return processBuilder.start();
    }
}
