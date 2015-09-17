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

package com.microsoft.applicationinsights.providers;

import com.google.gson.JsonSyntaxException;
import com.microsoft.applicationinsights.contracts.ContainerStatsMetric;

import java.io.BufferedReader;

/**
 * Created by yonisha on 7/23/2015.
 */
public class MetricProvider extends EventProvider<ContainerStatsMetric> {

    public MetricProvider(BufferedReader inputBuffer) {
        super(inputBuffer);
    }

    protected ContainerStatsMetric deserialize(String json) {
        ContainerStatsMetric containerStatsMetric = null;

        try {
            containerStatsMetric = new ContainerStatsMetric(json);
        } catch (JsonSyntaxException e) {
            System.out.println("Failed to deserialize JSON to container metric: " + json);
        }

        return containerStatsMetric;
    }
}
