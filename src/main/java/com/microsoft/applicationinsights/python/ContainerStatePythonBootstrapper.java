package com.microsoft.applicationinsights.python;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.microsoft.applicationinsights.providers.StateProvider;

/**
 * Created by yonisha on 8/5/2015.
 */
public class ContainerStatePythonBootstrapper extends PythonBootstrapper<StateProvider> {

    // region Members

    private static final String BOOTSTRAPPER_ARG = "events";

    // endregion Members

    // region Ctors

    public ContainerStatePythonBootstrapper(String... bootstrapperParams) {
        super(bootstrapperParams);
    }

    public ContainerStatePythonBootstrapper() {
        this(BOOTSTRAPPER_ARG);
    }

    // endregion Ctors

    // region Public

    @Override
    public StateProvider getResult() {
        return new StateProvider(new BufferedReader(new InputStreamReader(process.getInputStream())));
    }

    // endregion Public
}
