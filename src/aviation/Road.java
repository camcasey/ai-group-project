package aviation;

import coordinates.GeoCoordinate;
import coordinates.GpsCoordinate;
import coordinates.GeoLine;

public class Road extends LandingLocation {

    private final GeoCoordinate start;
    private final GeoCoordinate end;
    private final GeoLine line;
    public final int dist2;
    public final int head2;

    public Road(int dist1, int head1, GeoLine line, int dist2, int head2) {
        super(dist1, head1, "Road");
        this.line = line;
        this.start = line.start;
        this.end = line.end;
        this.dist2 = dist2;
        this.head2 = head2;
    }
    protected GeoCoordinate getStart() {return this.start;}

    protected GeoCoordinate getEnd() {return this.end;}

    @Override
    public String toString(){
        return super.toString() + String.format("Start, %d SM @ heading %d - End",dist2,head2);
    }

    public String getMapLink(){
        return String.format("http://www.google.com/maps/place/%f,%f", line.start.latitude, line.start.longitude);
    }
}
