package com.austinhodak.pubgcenter.weapons;

class WeaponsData {

    private static WeaponsData single_instance = null;

    public static WeaponsData getInstance() {
        if (single_instance == null) {
            single_instance = new WeaponsData();
        }

        return single_instance;
    }


}
