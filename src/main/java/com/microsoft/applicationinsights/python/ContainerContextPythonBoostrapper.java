package com.microsoft.applicationinsights.python;

/**
 * Created by yonisha on 7/28/2015.
 */
public class ContainerContextPythonBoostrapper extends PythonBootstrapper<Void> {

    private static final String BOOTSTRAPPER_ARG = "inject";

    public ContainerContextPythonBoostrapper() {
        super(BOOTSTRAPPER_ARG);
    }

    @Override
    public Object getResult() {
        return null;
    }
}
