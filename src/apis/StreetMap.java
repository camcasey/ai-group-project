package apis;

import coordinates.GeoCircle;
import coordinates.GeoCoordinate;
import coordinates.NodePath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Node;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/** Open Street map api calling
 * (Written by: Chris)
 * Wiki: https://wiki.openstreetmap.org/wiki/Overpass_API
 * Endpoint1: https://lz4.overpass-api.de/api/interpreter
 * Endpoint2: https://z.overpass-api.de/api/interpreter
 * Endpoint (Russian): https://maps.mail.ru/osm/tools/overpass/api/interpreter
 */
public class StreetMap {
    static final String ENDPOINT = "https://lz4.overpass-api.de/api/interpreter?";
    private GeoCircle bounds;

    private ArrayList<NodePath> roads;
    private static final double SM_METER = 1609.34; // Conversion factor from statute miles to meters

    public StreetMap(){
        roads = new ArrayList<>();
    }
    public void setBoundingCircle(GeoCircle bounds){
        this.bounds = bounds;
    }
    public ArrayList<NodePath> getRoadList(){
        return this.roads;
    }

    public void makeRequest(){
        String OverpassQL = String.format(
                "data=[out:json];way(around:%d,%f,%f)[\"highway\"~\"motorway|trunk|primary|secondary\"];(._;>;);out;",
                (int)(SM_METER*bounds.radius), bounds.latitude, bounds.longitude);

        //Path resp = ApiCall.postAndWriteResponseToFile(ENDPOINT, OverpassQL, "streetmapdata.json");
        String resp = ApiCall.post(ENDPOINT, OverpassQL);
        //Path resp = Path.of("streetmapdata.json");
        //JSONArray elements = parseElements(resp);
        JSONObject result = (JSONObject) JSONValue.parse(resp);
        JSONArray elements = (JSONArray) result.get("elements");

        List<JSONArray> paths = new ArrayList<>();
        HashMap<Long, GeoCoordinate> nodes = new HashMap<>();

        for(Object e: elements){
            JSONObject element = (JSONObject) e;
            switch ((String) element.get("type")){
                case "node" ->
                    nodes.put((Long) element.get("id"), new GeoCoordinate((double) element.get("lat"), (double) element.get("lon")));
                case "way" ->
                    paths.add((JSONArray) element.get("nodes"));
            }
        }
        for(JSONArray path: paths){
            List<GeoCoordinate> roadPoints = (List<GeoCoordinate>) path.stream().map(o -> {
                if(o instanceof Long index){
                    return nodes.get(index);
                }
                return null;
            }).collect(Collectors.toList());
            roads.add(new NodePath(roadPoints));
        }
    }
    private JSONArray parseElements(Path response){
        try{
            return (JSONArray)((JSONObject) new JSONParser().parse(new FileReader(response.toFile()))).get("elements");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
