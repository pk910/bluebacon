package de.dhbw.bluebacon.model;

public class BeaconData {
    public final String uuid;
    public final String major;
    public final String minor;
    public final double posX;
    public final double posY;
    public final int machineid;

    public BeaconData(String uuid, String major, String minor, double posX, double posY, int machineid){
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
        this.posX = posX;
        this.posY = posY;
        this.machineid = machineid;
    }
}
