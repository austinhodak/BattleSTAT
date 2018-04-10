package com.austinhodak.pubgcenter.damage_calculator;

import com.google.firebase.firestore.DocumentSnapshot;

public class DamageCalcPlayer {
    private DocumentSnapshot weapon, helmet, vest;

    public DocumentSnapshot getWeapon() {
        return weapon;
    }

    public void setWeapon(final DocumentSnapshot weapon) {
        this.weapon = weapon;
    }

    public DocumentSnapshot getHelmet() {
        return helmet;
    }

    public void setHelmet(final DocumentSnapshot helmet) {
        this.helmet = helmet;
    }

    public DocumentSnapshot getVest() {
        return vest;
    }

    public void setVest(final DocumentSnapshot vest) {
        this.vest = vest;
    }
}
