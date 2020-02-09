package com.brokenstrawapps.battlebuddy.utils

class Weapons {

    fun getCategory(id: String): CATEGORY {
        return when (id) {
            "Item_Weapon_AK47_C",
            "Item_Weapon_AUG_C",
            "Item_Weapon_BerylM762_C",
            "Item_Weapon_G36C_C",
            "Item_Weapon_Groza_C",
            "Item_Weapon_M16A4_C",
            "Item_Weapon_HK416_C",
            "Item_Weapon_Mk47Mutant_C",
            "Item_Weapon_QBZ95_C",
            "Item_Weapon_SCAR-L_C" -> CATEGORY.ASSAULT
            "Item_Weapon_AWM_C",
            "Item_Weapon_Kar98k_C",
            "Item_Weapon_M24_C",
            "Item_Weapon_Win1894_C" -> CATEGORY.SNIPER
            "Item_Weapon_Mini14_C",
            "Item_Weapon_Mk14_C",
            "Item_Weapon_QBU88_C",
            "Item_Weapon_SKS_C",
            "Item_Weapon_FNFal_C",
            "Item_Weapon_VSS_C" -> CATEGORY.DMR
            "Item_Weapon_MP5K_C",
            "Item_Weapon_UZI_C",
            "Item_Weapon_BizonPP19_C",
            "Item_Weapon_Thompson_C",
            "Item_Weapon_UMP_C",
            "Item_Weapon_Vector_C" -> CATEGORY.SMG
            "Item_Weapon_DP12_C",
            "Item_Weapon_Saiga12_C",
            "Item_Weapon_Winchester_C",
            "Item_Weapon_Berreta686_C",
            "Item_Weapon_Sawnoff_C" -> CATEGORY.SHOTGUN
            "Item_Weapon_DesertEagle_C",
            "Item_Weapon_FlareGun_C",
            "Item_Weapon_G18_C",
            "Item_Weapon_M1911_C",
            "Item_Weapon_M9_C",
            "Item_Weapon_NagantM1895_C",
            "Item_Weapon_Rhino_C",
            "Item_Weapon_vz61Skorpion_C" -> CATEGORY.PISTOL
            "Item_Weapon_DP28_C",
            "Item_Weapon_M249_C" -> CATEGORY.LMGs
            "Item_Weapon_FlashBang_C",
            "Item_Weapon_Grenade_C",
            "Item_Weapon_Grenade_Warmode_C",
            "Item_Weapon_Molotov_C",
            "Item_Weapon_SmokeBomb_C",
            "Item_Weapon_StickyGrenade_C",
            "Item_Weapon_SpikeTrap_C" -> CATEGORY.THROWABLE
            "Item_Weapon_Cowbar_C",
            "Item_Weapon_Machete_C",
            "Item_Weapon_Pan_C",
            "Item_Weapon_Sickle_C" -> CATEGORY.MELEE
            "Item_Weapon_Crossbow_C" -> CATEGORY.MISC
            else -> CATEGORY.MISC
        }
    }

    enum class CATEGORY(var title: String) {
        ASSAULT("Assault Rifles"),
        SNIPER("Sniper Rifles"),
        DMR("DMRs"),
        SMG("SMGs"),
        SHOTGUN("Shotguns"),
        PISTOL("Pistols"),
        LMGs("LMGs"),
        THROWABLE("Throwables"),
        MELEE("Melee"),
        MISC("Misc")
    }
}