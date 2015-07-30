package com.microsoft.applicationinsights.python;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by yonisha on 7/28/2015.
 */
public class ContainerContextPythonBoostrapper extends PythonBootstrapper<Void> {

    private static final String BOOTSTRAPPER_ARG = "container_context";

    public ContainerContextPythonBoostrapper() {
        super(BOOTSTRAPPER_ARG);
    }

    @Override
    public Object getResult() {
        return null;
    }
}
