package com.ahcjapps.battlebuddy.viewmodels.json;

import com.ahcjapps.battlebuddy.models.LogArmorDestroy;
import com.ahcjapps.battlebuddy.models.LogCarePackageLand;
import com.ahcjapps.battlebuddy.models.LogCarePackageSpawn;
import com.ahcjapps.battlebuddy.models.LogGamestatePeriodic;
import com.ahcjapps.battlebuddy.models.LogHeal;
import com.ahcjapps.battlebuddy.models.LogItemAttach;
import com.ahcjapps.battlebuddy.models.LogItemDetach;
import com.ahcjapps.battlebuddy.models.LogItemDrop;
import com.ahcjapps.battlebuddy.models.LogItemEquip;
import com.ahcjapps.battlebuddy.models.LogItemPickup;
import com.ahcjapps.battlebuddy.models.LogItemUnequip;
import com.ahcjapps.battlebuddy.models.LogItemUse;
import com.ahcjapps.battlebuddy.models.LogMatchDefinition;
import com.ahcjapps.battlebuddy.models.LogMatchEnd;
import com.ahcjapps.battlebuddy.models.LogMatchStart;
import com.ahcjapps.battlebuddy.models.LogPlayerAttack;
import com.ahcjapps.battlebuddy.models.LogPlayerCreate;
import com.ahcjapps.battlebuddy.models.LogPlayerKill;
import com.ahcjapps.battlebuddy.models.LogPlayerLogin;
import com.ahcjapps.battlebuddy.models.LogPlayerLogout;
import com.ahcjapps.battlebuddy.models.LogPlayerMakeGroggy;
import com.ahcjapps.battlebuddy.models.LogPlayerPosition;
import com.ahcjapps.battlebuddy.models.LogPlayerRevive;
import com.ahcjapps.battlebuddy.models.LogPlayerTakeDamage;
import com.ahcjapps.battlebuddy.models.LogSwimEnd;
import com.ahcjapps.battlebuddy.models.LogSwimStart;
import com.ahcjapps.battlebuddy.models.LogVehicleDestroy;
import com.ahcjapps.battlebuddy.models.LogVehicleLeave;
import com.ahcjapps.battlebuddy.models.LogVehicleRide;
import com.ahcjapps.battlebuddy.models.LogWheelDestroy;
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

