package com.brokenstrawapps.battlebuddy.viewmodels.json;

import com.brokenstrawapps.battlebuddy.models.LogArmorDestroy;
import com.brokenstrawapps.battlebuddy.models.LogCarePackageLand;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

public class MatchInterfaceDeserializer implements JsonDeserializer<MatchInterface> {

    @Override
    public MatchInterface deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jObject = (JsonObject) json;
        JsonElement typeObj = jObject.get("type");

        if(typeObj != null){
            String typeVal = typeObj.getAsString();
            //Timber.d(typeVal);

            switch (typeVal) {
                case "participant":
                    return context.deserialize(json, LogArmorDestroy.class);
                case "roster":
                    return context.deserialize(json, LogCarePackageLand.class);
                case "asset":
                    return context.deserialize(json, LogCarePackageLand.class);
            }
        }
        return null;
    }
}

