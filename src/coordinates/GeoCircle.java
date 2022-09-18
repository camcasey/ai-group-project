package coordinates;

public class GeoCircle extends GeoCoordinate{
    public final double radius;
    public GeoCircle(double latitude, double longitude, double radius){
        super(latitude, longitude);
        this.radius = radius;
    }
    public boolean intersects(GeoCircle other){
        return this.radius + other.radius >= this.distanceFeet(other);
    }

    @Override
    public String toString(){return super.toString() + String.format(" radius: %f SM",radius);}
}
