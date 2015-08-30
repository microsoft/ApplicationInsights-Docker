package com.microsoft.applicationinsights.python;

import com.microsoft.applicationinsights.providers.MetricProvider;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by yonisha on 7/28/2015.
 */
public class MetricCollectionPythonBoostrapper extends PythonBootstrapper<MetricProvider> {

    private static final String BOOTSTRAPPER_ARG = "collect";

    // region Ctors

    protected MetricCollectionPythonBoostrapper(ProcessBuilder processBuilder) {
        super(processBuilder);
    }

    public MetricCollectionPythonBoostrapper(String... bootstrapperParams) {
        super(bootstrapperParams);
    }

    public MetricCollectionPythonBoostrapper(int collectInterval) {
        this(BOOTSTRAPPER_ARG, generateCollectIntervalArgument(collectInterval));
    }

    // endregion Ctors

    // region Public methods

    @Override
    public MetricProvider getResult() {
        return new MetricProvider(new BufferedReader(new InputStreamReader(process.getInputStream())));
    }

    // endregion Public methods

    // region Private methods

    private static String generateCollectIntervalArgument(int collectInterval) {
        return String.format("--collect-interval=%s", String.valueOf(collectInterval));
    }

    // endregion Private methods
}
