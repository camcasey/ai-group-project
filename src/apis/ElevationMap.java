package apis;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import coordinates.GeoCoordinate;
import coordinates.GpsCoordinate;

/** Class: Elevation Map
 * (Written by: Chris)
 * This class is uses the api at https://www.open-elevation.com/ to obtain
 * elevation data. Call addLocation for each geoCoordinate you want the elevation for,
 * then call makeRequest(), which sends all coordinates in a single http request.
 * Then, any call to getElevation() should return a gps coordinate at that point.
 */
public class ElevationMap {
    HashMap<String, ArrayList<HashMap<String, Double>>> reqBody;
    HashMap<GeoCoordinate, GpsCoordinate> resolved;

    public ElevationMap(){
        reqBody = new HashMap<>(1);
        reqBody.put("locations", new ArrayList<>());
        this.resolved = new HashMap<>();
    }

    public void addLocation(GeoCoordinate coord){
        HashMap<String, Double> location = new HashMap<>(2);
        location.put("latitude", coord.latitude);
        location.put("longitude", coord.longitude);
        reqBody.get("locations").add(location);
    }

    public GpsCoordinate getElevation(GeoCoordinate coord){
        return resolved.get(coord);
    }

    public void makeRequest() {
        String body = JSONValue.toJSONString(this.reqBody);
        //System.out.println(body);

        String strResponse = ApiCall.post("https://api.open-elevation.com/api/v1/lookup", body);
        JSONObject response = (JSONObject) JSONValue.parse(strResponse);

        JSONArray results = (JSONArray) response.get("results");
        for(Object o: results){
            JSONObject result = (JSONObject) o;

            double latitude = (Double) result.get("latitude");
            double longitude = (Double) result.get("longitude");
            int elevation = ((Long) result.get("elevation")).intValue();

            this.resolved.put(
                    new GeoCoordinate(latitude, longitude),
                    new GpsCoordinate(latitude, longitude, elevation)
            );
        }
    }

    /**
     * This is an example usage of the Elevation map class.
     * You can run it from main to test.
     *
     * In the future, the usage could change depending on how we need our elevation data.
     * (i.e. do we need individual points? do we need a grid of elevations?
     *  or do we need a continuous field of elevations (a grid with interpolation)?)
     */
    public static void runExample(){
        //Make a new elevation map
        ElevationMap em = new ElevationMap();

        //Add each location that you would like an elevation for
        em.addLocation(new GeoCoordinate(10, 10));
        em.addLocation(new GeoCoordinate(20, 20));
        em.addLocation(new GeoCoordinate(41.161758, -8.583933));

        //Send the request to https://www.open-elevation.com/
        em.makeRequest();

        //Get the result of one coordinate in the request
        GpsCoordinate elevation = em.getElevation(new GeoCoordinate(10, 10));

        //Print it
        System.out.println(elevation);
    }
}
