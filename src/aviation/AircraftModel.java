package aviation;

import apis.ElevationMap;
import apis.Weather;
import coordinates.GeoCircle;
import coordinates.GeoCoordinate;
import coordinates.GpsCoordinate;

public class AircraftModel {

    private final String id;
    private final String type;
    private int glideSpeed;
    private GeoCoordinate coordinate;
    private int altitude;
    private int heading;
    private double airTime;
    private double descentRate;
    private static final int GLIDE_SPEED = 70; // ASSUMPTION based on typical single-engine descent rates [knots]
    private static final int DESCENT_RATE = 500; // ASSUMPTION based on typical single-engine descent rates [fpm]
    private static final double FRAME_AREA_M = 8.28*2.72; // ASSUMPTION taken from length*height of C172 [m^2]
    private static final double PLANE_MASS = 1157; // ASSUMPTION based on mass of C172 [kg]
    private static final double NM_SM = 1.1508; // conversion factor from nautical miles to statute miles
    private static final double FT_M = 1/3.281; // conversion factor from feet to meters
    private static final double MPH_MPS = 0.44704; // conversion factor from MPH to m/s
    private static final double KNOT_MPS = 1/1.94384; // conversion factor from knots to m/s
    private static final double LAT_M = 110000; // conversion factor from lat/long to meters

    public AircraftModel(String id, String type, GeoCoordinate coord, int alt, int heading){
        this.id = id;
        this.type = type;
        this.coordinate = coord;
        this.altitude = alt;
        this.heading = heading;
    }

    /**
     * I'm not sure how we'll have our ElevationMap objects laid out, we may only need to add coordinates here and make
     * the one call somewhere else?
     */

    /** needs to be tested */
    public double getGlideDistance(){
        ElevationMap ground = new ElevationMap();
        ground.addLocation(coordinate);
        ground.makeRequest();
        int groundElevation = ground.getElevation(coordinate).getElevation();
        int groundDist = this.altitude-groundElevation; // aircraft elevation above ground level (AGL)
        this.airTime = ((double) groundDist) / ((double) DESCENT_RATE); // time in minutes until plane is at the ground
        return GLIDE_SPEED*(airTime /((double)60))*NM_SM; // aircraft's range in SM from current altitude
        }

    public boolean isTooLow() {
        ElevationMap ground = new ElevationMap();
        ground.addLocation(coordinate);
        ground.makeRequest();
        int groundElevation = ground.getElevation(coordinate).getElevation();
        int groundDist = this.altitude-groundElevation; // aircraft elevation above ground level (AGL)
        if (groundDist <= 700) {return true;}
        return false;
    }

    public GeoCircle getWindCircle(){
        double dist = this.getGlideDistance(); // make sure this comes first to update airTime variable

        Weather wc = new Weather();
        wc.addLocation(this.coordinate);
        wc.makeRequest();

        int windHeading = (int) wc.getWeatherVal("wind_degree");
        double windMph = wc.getWeatherVal("wind_mph");

        Wind wind = new Wind(windHeading,windMph,this.coordinate);
        double windDensity = wind.getAirDensity(FT_M*this.altitude); // density of air in kg/m^3
        double area = FRAME_AREA_M;

        int theta = 180-Math.abs(windHeading-this.heading);
        if (theta < 0) {theta = theta + 360;}

        double effectiveArea = area*Math.sin(Math.toRadians(theta));

        int ltBound = this.heading + 315;
        while (ltBound >= 360) {ltBound = ltBound - 360;}
        int rtBound = this.heading + 45;
        while (rtBound >= 360) {rtBound = rtBound - 360;}
        int lbBound = this.heading + 225;
        while (lbBound >= 360) {lbBound = lbBound - 360;}
        int rbBound = this.heading + 135;
        while (rbBound >= 360) {rbBound = rbBound - 360;}

        if ((windHeading >= ltBound || windHeading <= rtBound) || (windHeading >= rbBound && windHeading <= lbBound)) {
            area = 1.2*2.72; // ASSUMPTION taken from width*height of C172 [m^2]
            effectiveArea = area*Math.cos(Math.toRadians(theta));
        }

        double windForce = effectiveArea*windDensity*(Math.pow((windMph*MPH_MPS),2)); // wind force in N
        double windAcceleration = windForce/PLANE_MASS; // acceleration of wind in wind direction
        double planeSpeed = GLIDE_SPEED*KNOT_MPS; // speed of aircraft in aircraft heading direction
        double windSpeedMPS = windMph*MPH_MPS; // speed of wind in m/s
        double correctionMag = Math.sqrt(Math.pow(planeSpeed,2)+Math.pow(windSpeedMPS,2)-(2*planeSpeed*windSpeedMPS*Math.cos(Math.toRadians(theta))));
        double alpha = Math.asin((windSpeedMPS/correctionMag)*Math.sin(Math.toRadians(theta)));
        int correctionHeading = this.heading - ((int)Math.toDegrees(alpha));
        if (correctionHeading<0) {correctionHeading = correctionHeading + 360;} // heading that circle will be shifted

        int planeCorrectionAngle = Math.abs(this.heading-correctionHeading); // angle between plane and correction
        double effectivePlaneSpeed = planeSpeed*Math.cos(Math.toRadians(planeCorrectionAngle));
        int windCorrectionAngle = Math.abs(windHeading-correctionHeading); // angle between wind and correction
        double effectiveAcceleration = windAcceleration*Math.cos(Math.toRadians(windCorrectionAngle));

        double x = 0.5*effectiveAcceleration*Math.pow(this.airTime*60,2) + (effectivePlaneSpeed*this.airTime*60); // distance in m shifted circle

        double newLon = x * Math.cos(Math.toRadians(correctionHeading));
        double newLat = x * Math.sin(Math.toRadians(correctionHeading));

        return new GeoCircle(coordinate.getLatitude()+(newLat/LAT_M),coordinate.getLongitude()+(newLon/LAT_M),dist);
    }

    protected String getId() {return this.id;}
    protected String getType(){return this.type;}

    protected int getGlideSpeed() {return this.glideSpeed;}

    public GeoCoordinate getCoordinate() {return this.coordinate;}

    protected int getAltitude() {return this.altitude;}

    protected int getHeading() {return this.heading;}

    protected double getDescentRate() {return this.descentRate;}

    public void setCoordinate(GeoCoordinate newCoord) {this.coordinate = newCoord;}

    public void setAltitude(int newAlt) {this.altitude = newAlt;}

    public void setHeading(int newHeading) {this.heading = newHeading;}

    @Override
    public String toString(){
        return String.format("%s - %s: %f°N %f°W, %dft above MSL, heading: %d", id, type, coordinate.getLatitude(), coordinate.getLongitude(),altitude,this.heading);
    }
}
