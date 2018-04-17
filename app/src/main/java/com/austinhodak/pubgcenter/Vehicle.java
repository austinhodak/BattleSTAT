package com.austinhodak.pubgcenter;

public class Vehicle {
    private String vehicleName, vehicleType, occupants, speed, health;
    private int image;

    public String getVehicleName() {
        return vehicleName;
    }

    public Vehicle setVehicleName(final String vehicleName) {
        this.vehicleName = vehicleName;
        return this;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public Vehicle setVehicleType(final String vehicleType) {
        this.vehicleType = vehicleType;
        return this;
    }

    public String getOccupants() {
        return occupants;
    }

    public Vehicle setOccupants(final String occupants) {
        this.occupants = occupants;
        return this;
    }

    public String getSpeed() {
        return speed;
    }

    public Vehicle setSpeed(final String speed) {
        this.speed = speed;
        return this;
    }

    public String getHealth() {
        return health;
    }

    public Vehicle setHealth(final String health) {
        this.health = health;
        return this;
    }

    public int getImage() {
        return image;
    }

    public Vehicle setImage(final int image) {
        this.image = image;
        return this;
    }
}
