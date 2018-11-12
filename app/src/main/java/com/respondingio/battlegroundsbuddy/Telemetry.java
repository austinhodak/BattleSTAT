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

    private String itemId = "{\"Item_Ammo_45ACP_C\": \".45 ACP Ammo\",\n"
            + "  \"Item_Ammo_556mm_C\": \"5.56mm Ammo\",\n"
            + "  \"Item_Ammo_762mm_C\": \"7.62mm Ammo\",\n"
            + "  \"Item_Ammo_9mm_C\": \"9mm Ammo\",\n"
            + "  \"Item_Ammo_Bolt_C\": \"Crossbow Bolt\",\n"
            + "  \"Item_Armor_C_01_Lv3_C\": \"Military Vest (Level 3)\",\n"
            + "  \"Item_Armor_D_01_Lv2_C\": \"Police Vest (Level 2)\",\n"
            + "  \"Item_Armor_E_01_Lv1_C\": \"Police Vest (Level 1)\",\n"
            + "  \"Item_Attach_Weapon_Lower_AngledForeGrip_C\": \"Angled Foregrip\",\n"
            + "  \"Item_Attach_Weapon_Lower_Foregrip_C\": \"Vertical Foregrip\",\n"
            + "  \"Item_Attach_Weapon_Lower_HalfGrip_C\": \"Half Grip\",\n"
            + "  \"Item_Attach_Weapon_Lower_LaserPointer_C\": \"Laser Sight\",\n"
            + "  \"Item_Attach_Weapon_Lower_LightweightForeGrip_C\": \"Light Grip\",\n"
            + "  \"Item_Attach_Weapon_Lower_QuickDraw_Large_Crossbow_C\": \"QuickDraw Crossbow Quiver\",\n"
            + "  \"Item_Attach_Weapon_Lower_ThumbGrip_C\": \"Thumb Grip\",\n"
            + "  \"Item_Attach_Weapon_Magazine_Extended_Large_C\": \"Large Extended Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_Extended_Medium_C\": \"Medium Extended Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_Extended_Small_C\": \"Small Extended Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_Extended_SniperRifle_C\": \"Sniper Rifle Extended Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Large_C\": \"Large Extended QuickDraw Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Medium_C\": \"Medium Extended QuickDraw Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_ExtendedQuickDraw_Small_C\": \"Small Extended QuickDraw Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_ExtendedQuickDraw_SniperRifle_C\": \"Sniper Rifle Extended QuickDraw Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_QuickDraw_Large_C\": \"Large QuickDraw Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_QuickDraw_Medium_C\": \"Medium Quickdraw Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_QuickDraw_Small_C\": \"Small Quickdraw Mag\",\n"
            + "  \"Item_Attach_Weapon_Magazine_QuickDraw_SniperRifle_C\": \"Sniper Rifle Quickdraw Mag\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_Choke_C\": \"Choke\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_Compensator_Large_C\": \"Large Compensator\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_Compensator_Medium_C\": \"Medium Compensator\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_Compensator_SniperRifle_C\": \"Sniper Rifle Compensator\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_Duckbill_C\": \"Duckbill\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_FlashHider_Large_C\": \"Large Flash Hider\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_FlashHider_Medium_C\": \"Medium Flash Hider\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_FlashHider_SniperRifle_C\": \"Sniper Rifle Flash Hider\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_Suppressor_Large_C\": \"Large Supressor\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_Suppressor_Medium_C\": \"Medium Supressor\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_Suppressor_Small_C\": \"Small Supressor\",\n"
            + "  \"Item_Attach_Weapon_Muzzle_Suppressor_SniperRifle_C\": \"Sniper Rifle Supressor\",\n"
            + "  \"Item_Attach_Weapon_Stock_AR_Composite_C\": \"Tactical Stock\",\n"
            + "  \"Item_Attach_Weapon_Stock_Shotgun_BulletLoops_C\": \"Shotgun Bullet Loops\",\n"
            + "  \"Item_Attach_Weapon_Stock_SniperRifle_BulletLoops_C\": \"Sniper Rifle Bullet Loops\",\n"
            + "  \"Item_Attach_Weapon_Stock_SniperRifle_CheekPad_C\": \"Sniper Rifle Cheek Pad\",\n"
            + "  \"Item_Attach_Weapon_Stock_UZI_C\": \"Uzi Stock\",\n"
            + "  \"Item_Attach_Weapon_Upper_ACOG_01_C\": \"4x ACOG Scope\",\n"
            + "  \"Item_Attach_Weapon_Upper_Aimpoint_C\": \"2x Aimpoint Scope\",\n"
            + "  \"Item_Attach_Weapon_Upper_CQBSS_C\": \"8x CQBSS Scope\",\n"
            + "  \"Item_Attach_Weapon_Upper_DotSight_01_C\": \"Red Dot Sight\",\n"
            + "  \"Item_Attach_Weapon_Upper_Holosight_C\": \"Holographic Sight\",\n"
            + "  \"Item_Attach_Weapon_Upper_PM2_01_C\": \"15x PM II Scope\",\n"
            + "  \"Item_Attach_Weapon_Upper_Scope3x_C\": \"3x Scope\",\n"
            + "  \"Item_Attach_Weapon_Upper_Scope6x_C\": \"6x Scope\",\n"
            + "  \"Item_Back_B_01_StartParachutePack_C\": \"Parachute\",\n"
            + "  \"Item_Back_C_01_Lv3_C\": \"Backpack (Level 3)\",\n"
            + "  \"Item_Back_C_02_Lv3_C\": \"Backpack (Level 3)\",\n"
            + "  \"Item_Back_E_01_Lv1_C\": \"Backpack (Level 1)\",\n"
            + "  \"Item_Back_E_02_Lv1_C\": \"Backpack (Level 1)\",\n"
            + "  \"Item_Back_F_01_Lv2_C\": \"Backpack (Level 2)\",\n"
            + "  \"Item_Back_F_02_Lv2_C\": \"Backpack (Level 2)\",\n"
            + "  \"Item_Boost_AdrenalineSyringe_C\": \"Adrenaline Syringe\",\n"
            + "  \"Item_Boost_EnergyDrink_C\": \"Energy Drink\",\n"
            + "  \"Item_Boost_PainKiller_C\": \"Painkiller\",\n"
            + "  \"Item_Ghillie_01_C\": \"Ghillie Suit\",\n"
            + "  \"Item_Ghillie_02_C\": \"Ghillie Suit\",\n"
            + "  \"Item_Head_E_01_Lv1_C\": \"Motorcycle Helmet (Level 1)\",\n"
            + "  \"Item_Head_E_02_Lv1_C\": \"Motorcycle Helmet (Level 1)\",\n"
            + "  \"Item_Head_F_01_Lv2_C\": \"Military Helmet (Level 2)\",\n"
            + "  \"Item_Head_F_02_Lv2_C\": \"Military Helmet (Level 2)\",\n"
            + "  \"Item_Head_G_01_Lv3_C\": \"Spetsnaz Helmet (Level 3)\",\n"
            + "  \"Item_Heal_Bandage_C\": \"Bandage\",\n"
            + "  \"Item_Heal_FirstAid_C\": \"First Aid Kit\",\n"
            + "  \"Item_Heal_MedKit_C\": \"Med kit\",\n"
            + "  \"Item_JerryCan_C\": \"Gas Can\",\n"
            + "  \"Item_Weapon_AK47_C\": \"AKM\",\n"
            + "  \"Item_Weapon_Apple_C\": \"Apple\",\n"
            + "  \"Item_Weapon_AUG_C\": \"AUG A3\",\n"
            + "  \"Item_Weapon_AWM_C\": \"AWM\",\n"
            + "  \"Item_Weapon_Berreta686_C\": \"S686\",\n"
            + "  \"Item_Weapon_BerylM762_C\": \"Beryl\",\n"
            + "  \"Item_Weapon_Cowbar_C\": \"Crowbar\",\n"
            + "  \"Item_Weapon_Crossbow_C\": \"Crossbow\",\n"
            + "  \"Item_Weapon_DP28_C\": \"DP-28\",\n"
            + "  \"Item_Weapon_FlashBang_C\": \"Flashbang\",\n"
            + "  \"Item_Weapon_FNFal_C\": \"SLR\",\n"
            + "  \"Item_Weapon_G18_C\": \"P18C\",\n"
            + "  \"Item_Weapon_Grenade_C\": \"Frag Grenade\",\n"
            + "  \"Item_Weapon_Grenade_Warmode_C\": \"Frag Grenade\",\n"
            + "  \"Item_Weapon_Groza_C\": \"Groza\",\n"
            + "  \"Item_Weapon_HK416_C\": \"M416\",\n"
            + "  \"Item_Weapon_Kar98k_C\": \"Kar98k\",\n"
            + "  \"Item_Weapon_M16A4_C\": \"M16A4\",\n"
            + "  \"Item_Weapon_M1911_C\": \"P1911\",\n"
            + "  \"Item_Weapon_M249_C\": \"M249\",\n"
            + "  \"Item_Weapon_M24_C\": \"M24\",\n"
            + "  \"Item_Weapon_M9_C\": \"P92\",\n"
            + "  \"Item_Weapon_Machete_C\": \"Machete\",\n"
            + "  \"Item_Weapon_Mini14_C\": \"Mini 14\",\n"
            + "  \"Item_Weapon_Mk14_C\": \"Mk14 EBR\",\n"
            + "  \"Item_Weapon_Mk47Mutant_C\": \"Mk47 Mutant\",\n"
            + "  \"Item_Weapon_Molotov_C\": \"Molotov Cocktail\",\n"
            + "  \"Item_Weapon_NagantM1895_C\": \"R1895\",\n"
            + "  \"Item_Weapon_Pan_C\": \"Pan\",\n"
            + "  \"Item_Weapon_QBU88_C\": \"QBU88\",\n"
            + "  \"Item_Weapon_QBZ95_C\": \"QBZ95\",\n"
            + "  \"Item_Weapon_Rhino_C\": \"R45\",\n"
            + "  \"Item_Weapon_Saiga12_C\": \"S12K\",\n"
            + "  \"Item_Weapon_Sawnoff_C\": \"Sawed-off\",\n"
            + "  \"Item_Weapon_SCAR-L_C\": \"SCAR-L\",\n"
            + "  \"Item_Weapon_Sickle_C\": \"Sickle\",\n"
            + "  \"Item_Weapon_SKS_C\": \"SKS\",\n"
            + "  \"Item_Weapon_SmokeBomb_C\": \"Smoke Grenade\",\n"
            + "  \"Item_Weapon_Thompson_C\": \"Tommy Gun\",\n"
            + "  \"Item_Weapon_UMP_C\": \"UMP9\",\n"
            + "  \"Item_Weapon_UZI_C\": \"Micro Uzi\",\n"
            + "  \"Item_Weapon_Vector_C\": \"Vector\",\n"
            + "  \"Item_Weapon_VSS_C\": \"VSS\",\n"
            + "  \"Item_Weapon_Win1894_C\": \"Win94\",\n"
            + "  \"Item_Weapon_Winchester_C\": \"S1897\",\n"
            + "  \"WarModeStartParachutePack_C\": \"Parachute\"}";

    public JSONObject getItemId() throws JSONException {
        return new JSONObject(itemId);
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
        object.put("war2", "War Mode");
        return object;
    }

    public JSONObject getRegion() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("xbox-as", "Xbox AS");
        object.put("xbox-eu", "Xbox EU");
        object.put("xbox-na", "Xbox NA");
        object.put("xbox-oc", "Xbox OC");
        object.put("xbox-sa", "Xbox SA");
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
        object.put("steam", "Steam");
        object.put("xbox", "Xbox");
        object.put("kakao", "Kakao");
        return object;
    }

    private JSONObject getItemIdIcons() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("Item_Weapon_Winchester_C", "gs://pubg-center.appspot.com/weapons/shotguns/Icon_weapon_Winchester.png");

        return object;
    }
}
