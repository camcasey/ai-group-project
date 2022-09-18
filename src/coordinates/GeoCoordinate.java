package coordinates;

public class GeoCoordinate {
    public final double latitude, longitude;

    public GeoCoordinate(double latitude, double longitude){
        latitude %= 180;
        longitude = longitude%360;
        if(latitude < -90){
            this.longitude = (longitude + 180)%360;
            this.latitude = -180-latitude;
        }else if(latitude > 90){
            this.longitude = (longitude + 180)%360;
            this.latitude = 180-latitude;
        }else{
            this.longitude = longitude;
            this.latitude = latitude;
        }
    }

    public double getLatitude() {return this.latitude;}

    public double getLongitude() {return this.longitude;}

    @Override
    public String toString(){
        return String.format("%f° N %f° W", latitude, longitude);
    }

    @Override
    public final boolean equals(Object o){
        if(o instanceof GeoCoordinate other){
            return other.latitude == this.latitude && other.longitude == this.longitude;
        }
        return false;
    }

    @Override
    public final int hashCode(){
        return (int)(longitude*1000000) + (int)(latitude*1000);
    }


    public double distanceFeet(GeoCoordinate other){
        double dLat = Math.toRadians(other.latitude-this.latitude);
        double dLon = Math.toRadians(other.longitude-this.longitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude)) * Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return 20902230.971129 * c; //Multiply by radius of the earth in feet
    }
    public double headingTo(GeoCoordinate other){
        //Units are degrees -> degrees
        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(other.latitude);
        double lon2 = Math.toRadians(other.longitude);

        double y = Math.sin(lon2 - lon1) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1);

        return Math.toDegrees(Math.atan2(y, x));
    }
    public double angleBetween(GeoCoordinate other1, GeoCoordinate other2){
        //units are degrees -> degrees
        return Math.abs(this.headingTo(other1) - other2.headingTo(this));
    }

}
