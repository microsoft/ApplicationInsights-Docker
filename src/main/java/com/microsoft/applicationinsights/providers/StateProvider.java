package com.microsoft.applicationinsights.providers;

import com.microsoft.applicationinsights.contracts.ContainerStateEvent;

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
        return null;
    }

    // endregion Private
}
