import apis.*;
import aviation.AircraftModel;
import aviation.Airport;
import aviation.Road;
import coordinates.GeoCircle;
import coordinates.GeoCoordinate;
import coordinates.GeoLine;
import coordinates.NodePath;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    /*
    public static void main(String[] args){
        // 1. Go to https://globe.adsbexchange.com
        // 2. Find and click on a plane to get its Registration number. (Reg.) Copy it.
        // 3. Run this program with that registration number as the argument.
        demo(args[0]);
    }
    */

    public static void main(String[] args) throws IOException {
        // I added the Json.simple library.
        // It's a very simple library that will let us read and write http requests, since
        //   it's pretty much always formatted as json.
        if(args.length == 0){
            demo("N685CD");
        }else{
            demo(args[0]);
        }
        System.exit(0);
        // This function gives an example of using the elevation map class.
        //ElevationMap.runExample();
        //StreetMap map = new StreetMap();
        //map.get(new GeoCoordinate(35.286669, -120.663543), 100);
        //map.show();
        GeoCircle boundingCircle = new GeoCircle(35.286669, -120.663543, 15); //example of 15 mile radius over downtown SLO

        // Parse command line arguments into an AircraftModel
        // get weather data, and calculate landing circle
        // Send landing center and radius to apis

        StreetMap streetMap = new StreetMap();
        streetMap.setBoundingCircle(boundingCircle);
        streetMap.makeRequest();
        Stream<GeoLine> potentialRoads = streetMap
                .getRoadList()
                .stream()
                .filter(path -> path.intoValidRoad(5.0, 2000))
                .map(GeoLine::new);
        PowerLines powerLines = new PowerLines();
        powerLines.setBoundingCircle(boundingCircle);
        powerLines.makeRequest();

        //potentialRoads = potentialRoads.filter(road -> powerLines.paths.stream().noneMatch(road::intersects));

        //potentialRoads.forEach(System.out::println);
        potentialRoads.forEach(road -> {
            System.out.println("Road:");
            System.out.printf("%f, %f\n", road.start.latitude, road.start.longitude);
            System.out.printf("%f, %f\n", road.end.latitude, road.end.longitude);
        });

        LandMap landMap = new LandMap();
        landMap.addLocation(boundingCircle);
        landMap.makeRequest(true, (boundingCircle.radius/69) * 2);// scale factor: 69 miles per 1 degree latitude

        // Flow is the following:

        // Give tail number to FlightAware api with makeAircraftRequest and get AircraftModel object
        // Use AircraftModel's location with Weather api
        // Call getWindCircle on AircraftModel
        // Glide distance is radius of new circle, call it as the range it into FlightAware api with addRange
        // Call makeAirportRequest to get list of Airport objects within range of the aircraft
        //

    }

    public static void demo(String tailString) throws IOException {

        // Api to a database which tracks information for all in-flight planes.
        FlightAware flightAware = new FlightAware();

        // A model of the aircraft we want to land.
        AircraftModel aircraftModel = flightAware.makeAircraftRequest(tailString);

        // Error checking - if the aircraft was not found, exit with an error message.
        if(aircraftModel == null){
            System.err.println("Unable to find plane given tail number through flightaware api");
            System.exit(-1);
        }

        // Check to see if aircraft is too low to make any specific landing location
        if (aircraftModel.isTooLow()) {
            System.out.println("Land straight ahead. Turning may result in significant loss of altitude.\n");
            System.exit(0);
        }

        // Using the Weather api and Elevation map api, get a circle encompassing all possible landing locations.
        GeoCircle potentialLandingZone = aircraftModel.getWindCircle();

        System.out.println("\nSearching for viable airports...");
        // Find all available airports within the landing circle.
        flightAware.addLocation(new GeoCoordinate(potentialLandingZone.latitude,potentialLandingZone.longitude));
        flightAware.addRange(potentialLandingZone.radius);
        ArrayList<Airport> airports = flightAware.makeAirportRequest();

        // Display all available airports
        if(airports.size()>0){
            System.out.println("\nHighest preference landing options available (Airports):");
            for(Airport airport: airports){
                System.out.println(airport.toString());
            }
        }else{
            System.out.println("None found.");
        }

        System.out.println("\nSearching for viable road landings...");
        // Look for any potential roads that can be landed on.
        double roadMaxAngle = 5.0;
        double roadMinLength = 2000;
        StreetMap streetMap = new StreetMap();
        streetMap.setBoundingCircle(potentialLandingZone);
        streetMap.makeRequest();
        Stream<GeoLine> potentialRoads = streetMap
                .getRoadList()
                .stream()
                .filter(path -> path.intoValidRoad(roadMaxAngle, roadMinLength))
                .map(GeoLine::new);

        // TODO: Make Street class to organize into LandingLocation as well

        // Get all power lines in the same area.
        PowerLines powerLines = new PowerLines();
        powerLines.setBoundingCircle(potentialLandingZone);
        powerLines.makeRequest();
        List<NodePath> powerLinePaths = powerLines.paths;

        // TODO: Filter out roads that intersect power lines
        // Note: We should leave this for after the presentation. The filtering criteria needs more work.
        /*
        potentialRoads = potentialRoads.filter((potentialRoad) -> {
            for(NodePath powerLine: powerLinePaths){
                if(potentialRoad.intersects(powerLine)){
                    return false;
                }
            }
            return true;
        });
        */

        // Display all available roads
        List<Road> validRoadsList = potentialRoads.map((road) -> {
            int distStart = (int) road.start.distanceFeet(aircraftModel.getCoordinate())/5280;
            int headStart = (int) aircraftModel.getCoordinate().headingTo(road.start);
            if (headStart < 0) {headStart = headStart + 360;}
            int distEnd = (int) road.end.distanceFeet(aircraftModel.getCoordinate())/5280;
            int headEnd = (int) aircraftModel.getCoordinate().headingTo(road.end);
            if (headEnd < 0) {headEnd = headEnd + 360;}
            if((distStart <= potentialLandingZone.radius)||(distEnd <= potentialLandingZone.radius)){
                return new Road(distStart,headStart,road,distEnd,headEnd);
            }else{
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if(validRoadsList.size()>0){
            System.out.println("\nMedium preference landing options available (Roads):");
            for(Road road: validRoadsList){
                System.out.println(road);
                System.out.printf("View road at %s \n", road.getMapLink());
            }
        }else{
            System.out.println("\nNone found.");
        }

        // Use landmap api to determine possible landings in fields and such
        LandMap landMap = new LandMap();
        landMap.addLocation(potentialLandingZone);
        landMap.makeRequest(true, (potentialLandingZone.radius/69) * 2);// scale factor: feet per 1 degree latitude


        // TODO: finish the landmap implementation
    }
}