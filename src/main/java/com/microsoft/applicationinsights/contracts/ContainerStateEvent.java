package com.microsoft.applicationinsights.contracts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yonisha on 8/5/2015.
 */
public class ContainerStateEvent {

    private String eventName;
    private Map<String, String> properties = new HashMap<String, String>();

    public ContainerStateEvent(String json) {
        this.deserialize(json);
    }

    public String getName() {
        return this.eventName;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    private void deserialize(String json) {
        JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
        this.eventName = jsonObj.get("name").getAsString();

        JsonObject propertiesObject = jsonObj.getAsJsonObject("properties");

        for (Map.Entry<String, JsonElement> kv : propertiesObject.entrySet()) {
            this.properties.put(kv.getKey(), kv.getValue().getAsString());
        }
    }
}
