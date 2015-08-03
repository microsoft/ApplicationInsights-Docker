package com.microsoft.applicationinsights.python;

import com.microsoft.applicationinsights.agent.MetricProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by yonisha on 7/28/2015.
 */
public class MetricCollectionPythonBoostrapper extends PythonBootstrapper<MetricProvider> {

    private static final String BOOTSTRAPPER_ARG = "collect";

    protected MetricCollectionPythonBoostrapper(ProcessBuilder processBuilder) {
        super(processBuilder);
    }

    public MetricCollectionPythonBoostrapper() {
        super(BOOTSTRAPPER_ARG);
    }

    @Override
    public MetricProvider getResult() {
        return new MetricProvider(new BufferedReader(new InputStreamReader(process.getInputStream())));
    }
}
