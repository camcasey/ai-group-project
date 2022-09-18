package coordinates;

import aviation.AircraftModel;

import java.util.ArrayList;
import java.util.List;

public class NodePath {
    public List<GeoCoordinate> nodes;

    public NodePath(List<GeoCoordinate> nodes){
        this.nodes = nodes;
    }

    public void println(){
        System.out.println("Path:");
        for(GeoCoordinate n: nodes){
            System.out.println(n);
        }
    }

    public boolean intoValidRoad(double maxAngle, double minLength){
        while(!nodes.isEmpty()){
            //Ensure that the path is long enough
            int endIndex = 0;
            for(double totalLength = 0; totalLength<minLength; endIndex++){
                endIndex+=1;
                if(endIndex>=nodes.size()){
                    nodes.clear();
                    return false;
                }
                totalLength += nodes.get(endIndex-1).distanceFeet(nodes.get(endIndex));
            }
            double totalAngle = 0;
            for(int i=2; i<endIndex; i++){
                double angle = nodes.get(i-1).angleBetween(nodes.get(i-2), nodes.get(i));
                totalAngle += angle;
            }
            if(totalAngle < maxAngle){
                for(int i=nodes.size()-1; i>endIndex; i--){
                    nodes.remove(i);
                }
                return true;
            }
            nodes.remove(0);
        }
        return false;
    }
    public GeoCircle boundingCircle(){
        GeoCoordinate center = this.nodes.get(this.nodes.size()/2);
        double maxDist = 0;
        for(GeoCoordinate node: nodes){
            double rad = center.distanceFeet(node);
            if(rad > maxDist){
                maxDist = rad;
            }
        }
        return new GeoCircle(center.latitude, center.longitude, maxDist);
    }
}
