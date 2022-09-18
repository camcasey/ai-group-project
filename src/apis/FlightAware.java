package apis;

import coordinates.GeoCoordinate;
import coordinates.GpsCoordinate;
import aviation.AircraftModel;
import aviation.Airport;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.lang.reflect.Array;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;

import static apis.ApiCall.client;

/** FlightAware Class
 * (Olivia May)
 * This class uses the FlightAware AeroAPI to obtain flight and airport information.
 */

public class FlightAware {
    HashMap<String, Double> reqbody;
    HashMap<String, Object> resolved; // FIGURE OUT HOW YOU WANT TO ORGANIZE THIS
    private static final String AIRPORT_KEY = "airports";
    private static final String AIRCRAFT_KEY = "aircraft";
    private static final String ID_KEY = "airport_code";
    private static final String LAT_KEY = "latitude";
    private static final String LONG_KEY = "longitude";
    private static final String ALT_KEY = "altitude";
    private static final String ELE_KEY = "elevation";
    private static final String NAME_KEY = "name";
    private static final String DIST_KEY = "distance";
    private static final String HEAD_KEY = "heading";
    private static final String TYPE_KEY = "aircraft_type";
    private static final String POS_KEY = "last_position";
    private static final String AEROAPI_KEY = "wFA2uOTUegtSJXlJLyjdJ5hZVtqePw5X"; // careful, it does charge me if you use it

    public FlightAware(){
        this.reqbody = new HashMap<>();
        this.resolved = new HashMap<>();
    }

    public void addLocation(GeoCoordinate geoCoord){
        reqbody.put("latitude", geoCoord.latitude);
        reqbody.put("longitude", geoCoord.longitude);
    }

    public void addRange(double glideDist) {reqbody.put("range", glideDist);} // statute miles

    private static String getApi(String url){
        try{
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("x-apikey", AEROAPI_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
            return "";
        }
    }

    public ArrayList<Airport> makeAirportRequest(){
        double lat = reqbody.get("latitude");
        double lon = reqbody.get("longitude");
        int range = reqbody.get("range").intValue();

        String query = String.format("https://aeroapi.flightaware.com/aeroapi/airports/nearby?latitude=%f&longitude=%f&radius=%d",lat,lon,range);
        String response = getApi(query);
        JSONObject result = (JSONObject) JSONValue.parse(response);
        Object status = result.get("status");
        if (status != null) {
            if((Long) status == 400){
            System.out.println("Invalid coordinates or range. Request could not be made.\n");
            return new ArrayList<>();}
        }
        JSONArray airports = (JSONArray) result.get(AIRPORT_KEY);
        ArrayList<Airport> airportList = new ArrayList<>();
        if (airports != null) {
            for (Object airport : airports) {
                double curLat = (double) (((JSONObject) airport).get(LAT_KEY));
                double curLon = (double) (((JSONObject) airport).get(LONG_KEY));
                double curEle = Double.parseDouble((((JSONObject) airport).get(ELE_KEY)).toString());
                double curDist = Double.parseDouble((((JSONObject) airport).get(DIST_KEY)).toString());
                double curHead = Double.parseDouble((((JSONObject) airport).get(HEAD_KEY)).toString());
                Airport curAirport = new Airport((int)curDist, (int)curHead, (String) ((JSONObject) airport).get(ID_KEY),
                        new GpsCoordinate(curLat, curLon, (int) curEle), (String) ((JSONObject) airport).get(NAME_KEY));
                airportList.add(curAirport);
            }
            resolved.put(AIRPORT_KEY,airportList);
        }
        return (ArrayList<Airport>) resolved.get(AIRPORT_KEY);
    }

    public AircraftModel makeAircraftRequest(String tail){

        String query = String.format("https://aeroapi.flightaware.com/aeroapi/flights/%s/position",tail);
        String response = getApi(query);
        JSONObject result = (JSONObject) JSONValue.parse(response);
        Object status = result.get("status");
        if (status != null) {
            if ((Long) status == 400){
            System.out.println("Invalid identifier or aircraft is not in flight. Request could not be made.\n");
            return null;}
        }
        String type = (String) result.get(TYPE_KEY);
        JSONObject lastPos = (JSONObject) result.get(POS_KEY);
        double lastLat = (double) lastPos.get(LAT_KEY);
        double lastLon = (double) lastPos.get(LONG_KEY);
        double lastAlt = Double.parseDouble(lastPos.get(ALT_KEY).toString());
        double lastHead = Double.parseDouble(lastPos.get(HEAD_KEY).toString());
        AircraftModel aircraft = new AircraftModel(tail,type,new GeoCoordinate(lastLat,lastLon),(int)lastAlt*100,(int)lastHead);
        resolved.put(AIRCRAFT_KEY, aircraft);
        return aircraft;
    }

    public static void runExample(){
        FlightAware fa = new FlightAware();

        fa.addLocation(new GeoCoordinate(38.492402,-122.709621));
        fa.addRange(25);

        fa.makeAirportRequest();

        System.out.println(fa.resolved.get(AIRPORT_KEY));
    }

}
