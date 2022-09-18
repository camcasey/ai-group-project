package apis;

import java.util.ArrayList;
import java.util.HashMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import coordinates.GeoCoordinate;

public class Weather {
    HashMap<String, Double> reqBody;
    HashMap<String, Double> resolved;

    public Weather(){
        this.reqBody = new HashMap<>();
        this.resolved = new HashMap<>();
    }

    public void addLocation(GeoCoordinate coord){
        reqBody.put("latitude", coord.latitude);
        reqBody.put("longitude", coord.longitude);
    }

    public double getWeatherVal(String key){
        return resolved.get(key);
    }

    public void makeRequest() {
        double lat = reqBody.get("latitude");
        double lon = reqBody.get("longitude");
        String key = "93a76f1d5f2143b282120033220405";

        String url = String.format("http://api.weatherapi.com/v1/current.json?key=%s&q=%f,%f&aqi=no", key, lat, lon);
        String strResponse = ApiCall.get(url);

        JSONObject response = (JSONObject) JSONValue.parse(strResponse);
        JSONObject current = (JSONObject) response.get("current");

        long wind_degree = (long) current.get("wind_degree");
        double wind_mph = (double) current.get("wind_mph");
        double gust_mph = (double) current.get("gust_mph");
        double vis_miles = (double) current.get("vis_miles");

        this.resolved.put("wind_degree", (double)wind_degree);
        this.resolved.put("wind_mph", wind_mph);
        this.resolved.put("gust_mph", gust_mph);
        this.resolved.put("vis_miles", vis_miles);
    }

    public void runExample(double lat, double lon){
        Weather wm = new Weather();
        wm.addLocation(new GeoCoordinate(lat, lon));
        wm.makeRequest();

        double wind_degree = wm.getWeatherVal("wind_degree");
        double wind_mph = wm.getWeatherVal("wind_mph");
        double gust_mph = wm.getWeatherVal("gust_mph");
        double vis_miles = wm.getWeatherVal("vis_miles");

        System.out.println("Success:");
        System.out.println("    wind degree: " + wind_degree);
        System.out.println("    wind mph: " + wind_mph);
        System.out.println("    gust mph: " + gust_mph);
        System.out.println("    visibility (miles): " + vis_miles);
    }
}