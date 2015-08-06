package com.microsoft.applicationinsights.providers;

import com.google.gson.JsonSyntaxException;
import com.microsoft.applicationinsights.contracts.ContainerStateEvent;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;

import java.io.BufferedReader;

/**
 * Created by yonisha on 8/5/2015.
 */
public class StateProvider extends EventProvider<ContainerStateEvent> {

    // region Ctor

    public StateProvider(BufferedReader bufferedReader) {
        super(bufferedReader);
    }

    // endregion Ctor

    // region Private

    @Override
    protected ContainerStateEvent deserialize(String json) {
        ContainerStateEvent containerStateEvent = null;

        try {
            containerStateEvent = new ContainerStateEvent(json);
        } catch (JsonSyntaxException e) {
            System.out.println("Failed to deserialize JSON to container state: " + json);
        }

        return containerStateEvent;
    }

    // endregion Private
}
