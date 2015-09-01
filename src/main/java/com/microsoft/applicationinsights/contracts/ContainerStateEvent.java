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

    // region Members

    private String eventName;
    private String ikey;
    private Map<String, String> properties = new HashMap<String, String>();

    // endregion Members

    // region Ctor

    public ContainerStateEvent(String json) {
        this.deserialize(json);
    }

    // endregion Ctor

    // region Public

    public String getName() {
        return this.eventName;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }

    public String getInstrumentationKey() {
        return ikey;
    }

    // endregion Public

    // region Private

    private void deserialize(String json) {
        JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
        this.eventName = jsonObj.get("name").getAsString();
        this.ikey = jsonObj.get("ikey").getAsString();

        JsonObject propertiesObject = jsonObj.getAsJsonObject("properties");

        for (Map.Entry<String, JsonElement> kv : propertiesObject.entrySet()) {
            this.properties.put(kv.getKey(), kv.getValue().getAsString());
        }
    }

    // endregion Private
}
