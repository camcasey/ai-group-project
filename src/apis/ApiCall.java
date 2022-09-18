package apis;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

/**
 * This class contains static utilities for making http post and get requests.
 */
public class ApiCall {
    static HttpClient client = HttpClient.newHttpClient();

    public static String post(String url, String body) {
        try{
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
            return "";
        }
    }

    public static Path postAndWriteResponseToFile(String url, String body, String filename) {

        try{
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            new File(filename).delete();
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(Path.of(filename)));
            return response.body();

        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
            return Path.of("");
        }
    }


    public static String authenticatedPost(String url, String body, String signedJwt) {
        try{
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + signedJwt)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response code: " + response.statusCode());


            return response.body();

        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
            return "";
        }
    }

    public static Path authenticatedPostAndWriteResponseToFile(String url, String body, String filename, String signedJwt) {
        try{
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + signedJwt)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(Path.of(filename)));
            return response.body();

        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
            return Path.of("");
        }
    }


    // TODO: not yet tested
    public static String get(String url){
        try{
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
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


    // TODO: not yet tested
    public static Path getAndWriteResponseToFile(String url, String filename){
        try{
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .GET()
                    .build();

            new File(filename).delete();
            HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(Path.of(filename)));

            return response.body();

        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
            return Path.of("");
        }
    }

    public static String authenticatedGet(String url, String signedJwt){
        try{
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + signedJwt)
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
}
