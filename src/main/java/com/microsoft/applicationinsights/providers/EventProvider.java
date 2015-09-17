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

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by yonisha on 8/5/2015.
 */
public abstract class EventProvider<T> {

    private BufferedReader inputBuffer;

    public EventProvider(BufferedReader inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public <T> T getNext() {
        String json;

        try {
            json = inputBuffer.readLine();
        } catch (IOException e) {
            System.out.println("Failed to read event from input stream with exception: " + e.getMessage());

            return null;
        }

        if (json == null || json.isEmpty()) {
            return null;
        }

        return deserialize(json);
    }

    // TODO: Implement a generic method and remove inherited classes.
    protected abstract <T> T deserialize(String json);
}
