package apis;

import coordinates.GeoCircle;
import coordinates.GeoCoordinate;
import coordinates.NodePath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

public class PowerLines {
    public final List<NodePath> paths;
    private GeoCoordinate min;
    private GeoCoordinate max;
    public PowerLines(){
        this.paths = new ArrayList<>();
    }
    public void setBoundingCircle(GeoCircle bounds){
        //Latitude feet per degrees average: 364002.6
        double delta_deg = bounds.radius / 364002.6;
        this.min = new GeoCoordinate(bounds.latitude - delta_deg, 0);
        this.max = new GeoCoordinate(bounds.latitude + delta_deg, 180);

        //Earth circumference at given latitude: delta_deg * cos(lat) feet
        double delta_lon_min = Math.cos(Math.toRadians(min.latitude));
        double delta_lon_max = Math.cos(Math.toRadians(max.latitude));
        if(delta_lon_min>0) delta_lon_min = delta_deg/delta_lon_min;
        else delta_lon_min = 90;
        if(delta_lon_max>0) delta_lon_max = delta_deg/delta_lon_max;
        else delta_lon_max = 90;

        this.min = new GeoCoordinate(this.min.latitude, bounds.longitude - delta_lon_min);
        this.max = new GeoCoordinate(this.max.latitude, bounds.longitude + delta_lon_max);
    }
    public void makeRequest(){
        String geometry = String.format("%f%s%f%s%f%s%f", min.longitude, "%2C", min.latitude, "%2C", max.longitude, "%2C", max.latitude);
        String query = "https://services1.arcgis.com/Hp6G80Pky0om7QvQ/arcgis/rest/services/Transmission_Lines_gdb/FeatureServer/0/query?where=1%3D1&outFields=&geometry=" + geometry + "&geometryType=esriGeometryEnvelope&inSR=4326&spatialRel=esriSpatialRelIntersects&outSR=4326&f=json";
        //Path resp = ApiCall.getAndWriteResponseToFile(query, "powerlines.json");
        Path resp = Path.of("powerlines.json");
        JSONArray features = parseFeatures(resp);
        for(Object i: features){
            JSONArray paths = (JSONArray) ((JSONObject) ((JSONObject) i).get("geometry")).get("paths");
            for(Object nodePath: paths){
                this.paths.add(new NodePath((List) ((JSONArray) nodePath).stream().map(o -> {
                    JSONArray arr = (JSONArray) o;
                    return new GeoCoordinate(jsonObjectAsDouble(arr.get(1)), jsonObjectAsDouble(arr.get(0)));
                }).collect(Collectors.toList())));
            }
        }
    }
    private double jsonObjectAsDouble(Object o){
        try{
            return (Double) o;
        }catch (ClassCastException e){
            return (Long) o;
        }
    }
    private JSONArray parseFeatures(Path response){
        try{
            return (JSONArray)((JSONObject) new JSONParser().parse(new FileReader(response.toFile()))).get("features");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
