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

    protected abstract <T> T deserialize(String json);
}
