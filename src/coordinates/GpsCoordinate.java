package coordinates;

public class GpsCoordinate extends GeoCoordinate {
    int elevation;
    public GpsCoordinate(double latitude, double longitude, int elevation) {
        super(latitude, longitude);
        this.elevation = elevation;
    }
    public int getElevation() {return this.elevation;}

    // MAKE SURE UNITS ARE IN FT
    @Override
    public String toString(){
        return String.format("%f°N %f°W, %dft", latitude, longitude, elevation);
    }

}