package coordinates;

public class GeoLine {
    public final GeoCoordinate start;
    public final GeoCoordinate end;
    public GeoLine(GeoCoordinate start, GeoCoordinate end){
        this.start = start;
        this.end = end;
    }
    public GeoLine(NodePath path){
        this.start = path.nodes.get(0);
        this.end = path.nodes.get(path.nodes.size()-1);
    }
    public boolean intersects(GeoCoordinate a, GeoCoordinate b){
        //TODO: Write a function to compute whether two lines intersect each other.
        return false;
    }
    public String toString(){
        return String.format("From %s to %s", this.start, this.end);
    }


    public GeoCircle boundingCircle(){
        GeoCoordinate center = new GeoCoordinate(
                (this.end.latitude+this.start.latitude)*0.5,
                (this.end.longitude+this.start.longitude)*0.5
        );
        return new GeoCircle(center.latitude, center.longitude, this.start.distanceFeet(center));
    }
    public double heading(){
        return this.start.headingTo(this.end);
    }
    public boolean intersects(GeoLine line) {
        //TODO: write a function to determine whether two lines intersect.
        return false;
    }

    public boolean intersects(NodePath path){
        if(this.boundingCircle().intersects(path.boundingCircle())){
            for(int i=1; i<path.nodes.size(); i++){
                if(this.intersects(new GeoLine(path.nodes.get(i-1), path.nodes.get(i)))){
                    return true;
                }
            }
        }
        return false;
    }
}
