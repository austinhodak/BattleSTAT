package com.respondingio.battlegroundsbuddy;

import org.json.JSONException;
import org.json.JSONObject;

public class Telemetry {
    private String damageTypeCategory = "{\n" +
            "  \"Damage_BlueZone\": \"Bluezone Damage\",\n" +
            "  \"Damage_Drown\": \"Drowning Damage\",\n" +
            "  \"Damage_Explosion_Grenade\": \"Grenade Explosion Damage\",\n" +
            "  \"Damage_Explosion_RedZone\": \"Redzone Explosion Damage\",\n" +
            "  \"Damage_Explosion_Vehicle\": \"Vehicle Explosion Damage\",\n" +
            "  \"Damage_Groggy\": \"Bleed out damage\",\n" +
            "  \"Damage_Gun\": \"Gun Damage\",\n" +
            "  \"Damage_Instant_Fall\": \"Fall Damage\",\n" +
            "  \"Damage_Melee\": \"Melee Damage\",\n" +
            "  \"Damage_Molotov\": \"Molotov Damage\",\n" +
            "  \"Damage_VehicleCrashHit\": \"Vehicle Crash Damage\",\n" +
            "  \"Damage_VehicleHit\": \"Vehicle Damage\"\n" +
            "}";

    public JSONObject getDamageTypeCategory() throws JSONException {
        return new JSONObject(damageTypeCategory);
    }

    private String damageCauserName = "{\n" +
            "  \"AquaRail_A_01_C\": \"Aquarail\",\n" +
            "  \"AquaRail_A_02_C\": \"Aquarail\",\n" +
            "  \"AquaRail_A_03_C\": \"Aquarail\",\n" +
            "  \"BattleRoyaleModeController_Def_C\": \"Bluezone\",\n" +
            "  \"BattleRoyaleModeController_Desert_C\": \"Bluezone\",\n" +
            "  \"BattleRoyaleModeController_Savage_C\": \"Bluezone\",\n" +
            "  \"Boat_PG117_C\": \"PG-117\",\n" +
            "  \"BP_M_Rony_A_01_C\": \"Rony\",\n" +
            "  \"BP_M_Rony_A_02_C\": \"Rony\",\n" +
            "  \"BP_M_Rony_A_03_C\": \"Rony\",\n" +
            "  \"BP_Mirado_A_02_C\": \"Mirado\",\n" +
            "  \"BP_Mirado_Open_03_C\": \"Mirado (open top)\",\n" +
            "  \"BP_Mirado_Open_04_C\": \"Mirado (open top)\",\n" +
            "  \"BP_MolotovFireDebuff_C\": \"Molotov Fire Damage\",\n" +
            "  \"BP_Motorbike_04_C\": \"Motorcycle\",\n" +
            "  \"BP_Motorbike_04_Desert_C\": \"Motorcycle\",\n" +
            "  \"BP_Motorbike_04_SideCar_C\": \"Motorcycle (w/ Sidecar)\",\n" +
            "  \"BP_Motorbike_04_SideCar_Desert_C\": \"Motorcycle (w/ Sidecar)\",\n" +
            "  \"BP_PickupTruck_A_01_C\": \"Pickup Truck (closed top)\",\n" +
            "  \"BP_PickupTruck_A_02_C\": \"Pickup Truck (closed top)\",\n" +
            "  \"BP_PickupTruck_A_03_C\": \"Pickup Truck (closed top)\",\n" +
            "  \"BP_PickupTruck_A_04_C\": \"Pickup Truck (closed top)\",\n" +
            "  \"BP_PickupTruck_A_05_C\": \"Pickup Truck (closed top)\",\n" +
            "  \"BP_PickupTruck_B_01_C\": \"Pickup Truck (open top)\",\n" +
            "  \"BP_PickupTruck_B_02_C\": \"Pickup Truck (open top)\",\n" +
            "  \"BP_PickupTruck_B_03_C\": \"Pickup Truck (open top)\",\n" +
            "  \"BP_PickupTruck_B_04_C\": \"Pickup Truck (open top)\",\n" +
            "  \"BP_PickupTruck_B_05_C\": \"Pickup Truck (open top)\",\n" +
            "  \"BP_Scooter_01_A_C\": \"Scooter\",\n" +
            "  \"BP_Scooter_02_A_C\": \"Scooter\",\n" +
            "  \"BP_Scooter_03_A_C\": \"Scooter\",\n" +
            "  \"BP_Scooter_04_A_C\": \"Scooter\",\n" +
            "  \"BP_Van_A_01_C\": \"Van\",\n" +
            "  \"BP_Van_A_02_C\": \"Van\",\n" +
            "  \"BP_Van_A_03_C\": \"Van\",\n" +
            "  \"Buff_DecreaseBreathInApnea_C\": \"Drowning\",\n" +
            "  \"Buggy_A_01_C\": \"Buggy\",\n" +
            "  \"Buggy_A_02_C\": \"Buggy\",\n" +
            "  \"Buggy_A_03_C\": \"Buggy\",\n" +
            "  \"Buggy_A_04_C\": \"Buggy\",\n" +
            "  \"Buggy_A_05_C\": \"Buggy\",\n" +
            "  \"Buggy_A_06_C\": \"Buggy\",\n" +
            "  \"Dacia_A_01_v2_C\": \"Dacia\",\n" +
            "  \"Dacia_A_02_v2_C\": \"Dacia\",\n" +
            "  \"Dacia_A_03_v2_C\": \"Dacia\",\n" +
            "  \"Dacia_A_04_v2_C\": \"Dacia\",\n" +
            "  \"None\": \"None\",\n" +
            "  \"PG117_A_01_C\": \"PG-117\",\n" +
            "  \"PlayerFemale_A_C\": \"Player\",\n" +
            "  \"PlayerMale_A_C\": \"Player\",\n" +
            "  \"ProjGrenade_C\": \"Frag Grenade\",\n" +
            "  \"ProjMolotov_C\": \"Molotov Cocktail\",\n" +
            "  \"ProjMolotov_DamageField_Direct_C\": \"Molotov Cocktail Fire Field\",\n" +
            "  \"RedZoneBomb_C\": \"Redzone\",\n" +
            "  \"Uaz_A_01_C\": \"UAZ (open top)\",\n" +
            "  \"Uaz_B_01_C\": \"UAZ (soft top)\",\n" +
            "  \"Uaz_C_01_C\": \"UAZ (hard top)\",\n" +
            "  \"WeapAK47_C\": \"AKM\",\n" +
            "  \"WeapAUG_C\": \"AUG A3\",\n" +
            "  \"WeapAWM_C\": \"AWM\",\n" +
            "  \"WeapBerreta686_C\": \"S686\",\n" +
            "  \"WeapBerylM762_C\": \"Beryl\",\n" +
            "  \"WeapCowbar_C\": \"Crowbar\",\n" +
            "  \"WeapCrossbow_1_C\": \"Crossbow\",\n" +
            "  \"WeapDP28_C\": \"DP-28\",\n" +
            "  \"WeapFNFal_C\": \"SLR\",\n" +
            "  \"WeapG18_C\": \"P18C\",\n" +
            "  \"WeapGroza_C\": \"Groza\",\n" +
            "  \"WeapHK416_C\": \"M416\",\n" +
            "  \"WeapKar98k_C\": \"Kar98k\",\n" +
            "  \"WeapM16A4_C\": \"M16A4\",\n" +
            "  \"WeapM1911_C\": \"P1911\",\n" +
            "  \"WeapM249_C\": \"M249\",\n" +
            "  \"WeapM24_C\": \"M24\",\n" +
            "  \"WeapM9_C\": \"P92\",\n" +
            "  \"WeapMachete_C\": \"Machete\",\n" +
            "  \"WeapMini14_C\": \"Mini 14\",\n" +
            "  \"WeapMk14_C\": \"Mk14 EBR\",\n" +
            "  \"WeapMk47Mutant_C\": \"Mk47 Mutant\",\n" +
            "  \"WeapNagantM1895_C\": \"R1895\",\n" +
            "  \"WeapPan_C\": \"Pan\",\n" +
            "  \"WeapQBU88_C\": \"QBU88\",\n" +
            "  \"WeapQBZ95_C\": \"QBZ95\",\n" +
            "  \"WeapRhino_C\": \"R45\",\n" +
            "  \"WeapSaiga12_C\": \"S12K\",\n" +
            "  \"WeapSawnoff_C\": \"Sawed-off\",\n" +
            "  \"WeapSCAR-L_C\": \"SCAR-L\",\n" +
            "  \"WeapSickle_C\": \"Sickle\",\n" +
            "  \"WeapSKS_C\": \"SKS\",\n" +
            "  \"WeapThompson_C\": \"Tommy Gun\",\n" +
            "  \"WeapUMP_C\": \"UMP9\",\n" +
            "  \"WeapUZI_C\": \"Micro Uzi\",\n" +
            "  \"WeapVector_C\": \"Vector\",\n" +
            "  \"WeapVSS_C\": \"VSS\",\n" +
            "  \"WeapWin94_C\": \"Win94\",\n" +
            "  \"WeapWinchester_C\": \"S1897\"\n" +
            "}";

    public JSONObject getDamageCauserName() throws JSONException {
        return new JSONObject(damageCauserName);
    }

    public JSONObject getGameModes() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("duo", "Duo TPP");
        object.put("duo-fpp", "Duo FPP");
        object.put("solo", "Solo TPP");
        object.put("solo-fpp", "Solo FPP");
        object.put("squad", "Squad TPP");
        object.put("squad-fpp", "Squad FPP");
        object.put("normal-duo", "Duo TPP");
        object.put("normal-duo-fpp", "Duo FPP");
        object.put("normal-solo", "Solo TPP");
        object.put("normal-solo-fpp", "Solo FPP");
        object.put("normal-squad", "Squad TPP");
        object.put("normal-squad-fpp", "Squad FPP");
        return object;
    }

    public JSONObject getRegion() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("xbox-as", "Xbox AS");
        object.put("xbox-eu", "Xbox EU");
        object.put("xbox-na", "Xbox NA");
        object.put("xbox-oc", "Xbox OC");
        object.put("pc-krjp", "Xbox KRJP");
        object.put("pc-jp", "PC JP");
        object.put("pc-na", "PC NA");
        object.put("pc-eu", "PC EU");
        object.put("pc-ru", "PC RU");
        object.put("pc-oc", "PC OC");
        object.put("pc-kakao", "PC KAKAO");
        object.put("pc-sea", "PC SEA");
        object.put("pc-sa", "PC SA");
        object.put("pc-as", "PC AS");
        object.put("pc-tournament", "PC TOURNEY");
        return object;
    }
}
