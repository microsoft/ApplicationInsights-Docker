/*
 * ApplicationInsights-Docker
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

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
