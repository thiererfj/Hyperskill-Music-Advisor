package advisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class Authorizer {

    private AdvisorEngine advisorEngine;
    private static String userAuthCode;
    private static String userAccessToken = "";
    private static final String CLIENT_ID = "f30a2058c2144488b71c0847cdc12fec";
    private static final String CLIENT_SECRET = "1a50e396372c4c95a327e33423f5195d";
    private static final String REDIRECT_URI = "http://localhost:8080";
    private static final String RESPONSE_TYPE = "code";
    private static final String GRANT_TYPE = "authorization_code";
    private static HttpClient httpClient;
    private static HttpServer httpServer;
    private static HttpResponse<String> httpResponse;
    private String authServerPath;
    private static final String DEFAULT_AUTH_SERVER_PATH = "https://accounts.spotify.com";


    public Authorizer(String accessArg, AdvisorEngine advisorEngine) {
        this.advisorEngine = advisorEngine;

        if (!Objects.isNull(accessArg)) {
            authServerPath = accessArg;
        } else {
            authServerPath = DEFAULT_AUTH_SERVER_PATH;
        }

        httpClient = HttpClient.newBuilder().build();
    }

    public boolean authorizeUser() throws IOException, InterruptedException {
        System.out.println("use this link to request the access code: ");
        System.out.println(authServerPath
                + "/authorize"
                + "?client_id=" + CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI
                + "&response_type=" + RESPONSE_TYPE);
        System.out.println("waiting for code...");
        requestAccessCode();
        httpResponse = requestAccessToken();

        try {
            if (httpResponse.statusCode() == 200) {
                JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
                userAccessToken = jsonObject.get("access_token").getAsString();
                advisorEngine.setUserAccessToken(userAccessToken);
                System.out.println("response: ");
                System.out.println(userAccessToken);

                return true;
            }
        } catch (NullPointerException e) {
            return false;
        }

        return false;
    }

    public void requestAccessCode() {
        try {
            httpServer = HttpServer.create();
            httpServer.bind(new InetSocketAddress(8080), 0);
            httpServer.start();
            System.out.println("test start server");

            httpServer.createContext("/", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String response;

                if (query != null && query.contains("code")) {
                    userAuthCode = query.substring(5);
                    System.out.println("code received");
                    System.out.println(userAuthCode);
                    System.out.println("making http request for access_token...");
                    response = "Got the code. Return back to your program.";

                } else {
                    response = "Authorization code not found. Try again.";
                }

                exchange.sendResponseHeaders(200, query.length());
                exchange.getResponseBody().write(response.getBytes());
                exchange.getResponseBody().close();
            });

            if (userAuthCode == null) {
                Thread.sleep(500);
            }

            httpServer.stop(10);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("server error");
        }
    }

    private HttpResponse<String> requestAccessToken() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(authServerPath + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "&grant_type=" + GRANT_TYPE
                                + "&code=" + userAuthCode
                                + "&client_id=" + CLIENT_ID
                                + "&client_secret=" + CLIENT_SECRET
                                + "&redirect_uri=" + REDIRECT_URI))
                .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

}
