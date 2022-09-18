package aviation;

public class LandingLocation {
    private final int distance;
    private final int heading;
    private final String type;

    public LandingLocation(int dist, int head, String type){
        this.distance = dist;
        this.heading = head;
        this.type = type;
    }

    protected int getDistance(){return this.distance;}

    protected int getHeading(){return this.heading;}

    protected String getType(){return this.type;}

    @Override
    public String toString(){return String.format("%s %d SM @ heading %d - ",type,distance,heading);}

}
