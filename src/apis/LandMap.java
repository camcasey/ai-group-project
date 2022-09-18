package apis;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.auth.oauth2.ServiceAccountCredentials;
import coordinates.GeoCoordinate;

import java.io.*;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

//safeflight@grounded-access-351304.iam.gserviceaccount.com
//src/grounded-access-351304-73375a0d5004.json

public class LandMap {
    HashMap<String, Double> reqBody;
    HashMap<String, Double> resolved;

    public LandMap(){
        this.reqBody = new HashMap<>();
        this.resolved = new HashMap<>();
    }

    public void addLocation(GeoCoordinate coord){
        reqBody.put("latitude", coord.latitude);
        reqBody.put("longitude", coord.longitude);
    }
    /**
     * Generates a signed JSON Web Token using a Google API Service Account
     * utilizes com.auth0.jwt.
     */
    public static String generateJwt(final String saKeyfile, final String saEmail,
                                     final String audience, final int expiryLength)
            throws FileNotFoundException, IOException {

        Date now = new Date();
        Date expTime = new Date(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expiryLength));

        // Build the JWT payload
        JWTCreator.Builder token = JWT.create()
                .withIssuedAt(now)
                // Expires after 'expiryLength' seconds
                .withExpiresAt(expTime)
                // Must match 'issuer' in the security configuration in your
                // swagger spec (e.g. service account email)
                .withIssuer(saEmail)
                // Must be either your Endpoints service name, or match the value
                // specified as the 'x-google-audience' in the OpenAPI document
                .withAudience(audience)
                // Subject and email should match the service account's email
                .withSubject(saEmail)
                .withClaim("email", saEmail);

        // Sign the JWT with a service account
        FileInputStream stream = new FileInputStream(saKeyfile);
        ServiceAccountCredentials cred = ServiceAccountCredentials.fromStream(stream);
        RSAPrivateKey key = (RSAPrivateKey) cred.getPrivateKey();
        Algorithm algorithm = Algorithm.RSA256(null, key);
        return token.sign(algorithm);
    }

    /**
     * Generates a signed JSON Web Token using a Google API Service Account
     * utilizes com.auth0.jwt.
     */
    public void makeRequest(boolean fullColor, double scaleFactor) throws IOException {
        int expLength = 1000;
        double lat = reqBody.get("latitude");
        double lon = reqBody.get("longitude");

        String jsonPath = "src/grounded-access-351304-73375a0d5004.json";
        String email = "safeflight@grounded-access-351304.iam.gserviceaccount.com";
        String serviceName = "https://earthengine.googleapis.com/";
        String pngFile = "src/img.png";
        String dataUrl = "https://earthengine.googleapis.com/v1alpha/projects/earthengine-public/assets/ESA/WorldCover/v100/2020:getPixels";
        String pngBody = String.format("{\"fileFormat\":\"PNG\",\"bandIds\":[\"Map\"],\"region\":{\"type\":\"Polygon\"," +
                "\"coordinates\":[[[%f, %f],[%f, %f],[%f, %f],[%f, %f],[%f, %f]]]},\"grid\":{\"dimensions\":" +
                "{\"width\":2048,\"height\":2048}},\"visualizationOptions\":{\"ranges\":" +
                "[{\"min\":10,\"max\":100}],\"paletteColors\":[\"a3a3a3\",\"ffbb22\"," +
                "\"ffff4c\",\"f096ff\",\"a3a3a3\",\"a3a3a3\",\"a3a3a3\",\"0064c8\",\"a3a3a3\"," +
                "\"a3a3a3\",\"a3a3a3\"]}}", lon - (0.5 * scaleFactor), lat - (0.5 * scaleFactor),
                lon + (0.5 * scaleFactor), lat - (0.5 * scaleFactor), lon + (0.5 * scaleFactor),
                lat + (0.5 * scaleFactor), lon - (0.5 * scaleFactor), lat + (0.5 * scaleFactor),
                lon - (0.5 * scaleFactor), lat - (0.5 * scaleFactor));
        if(fullColor){
            pngBody = String.format("{\"fileFormat\":\"PNG\",\"bandIds\":[\"Map\"],\"region\":{\"type\":\"Polygon\"," +
                    "\"coordinates\":[[[%f, %f],[%f, %f],[%f, %f],[%f, %f],[%f, %f]]]},\"grid\":{\"dimensions\":" +
                    "{\"width\":2048,\"height\":2048}},\"visualizationOptions\":{\"ranges\":" +
                    "[{\"min\":10,\"max\":100}],\"paletteColors\":[\"006400\",\"ffbb22\"," +
                    "\"ffff4c\",\"f096ff\",\"fa0000\",\"b4b4b4\",\"f0f0f0\",\"0064c8\",\"0096a0\"," +
                    "\"00cf75\",\"fae6a0\"]}}", lon - (0.5 * scaleFactor), lat - (0.5 * scaleFactor),
                    lon + (0.5 * scaleFactor), lat - (0.5 * scaleFactor), lon + (0.5 * scaleFactor),
                    lat + (0.5 * scaleFactor), lon - (0.5 * scaleFactor), lat + (0.5 * scaleFactor),
                    lon - (0.5 * scaleFactor), lat - (0.5 * scaleFactor));
        }

        //System.out.println("generating JWT...");

        String signedJWT = generateJwt(jsonPath, email, serviceName, expLength);

        //System.out.println("    " + signedJWT + "\ngetting image...");

        ApiCall.authenticatedPostAndWriteResponseToFile(dataUrl, pngBody, pngFile, signedJWT);

        System.out.println("\nWrote surrounding terrian data to img.png");
    }
}