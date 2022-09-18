package aviation;

import coordinates.GpsCoordinate;

public class Airport extends LandingLocation {

    private final String id;
    private final GpsCoordinate gpsLocation;
    private final String name;

    public Airport(int dist, int head, String id, GpsCoordinate gps, String name){
        super(dist,head,"Airport");
        this.id = id;
        this.gpsLocation = gps;
        this.name = name;
    }

    protected String getId() {return this.id;}

    protected GpsCoordinate getGpsLocation() {return this.gpsLocation;}

    protected String getName() {return this.name;}

    @Override
    public String toString(){
        return super.toString() + String.format("(%s) %s: %s above MSL", id, name, gpsLocation);
    }
}
