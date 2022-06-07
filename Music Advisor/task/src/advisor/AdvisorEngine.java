package advisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdvisorEngine {

    private static final String DEFAULT_API_SERVER_PATH = "https://api.spotify.com";
    private static String APIServerPath;
    private String userAccessToken;
    private static HttpClient httpClient;
    private static HttpResponse<String> httpResponse;

    public AdvisorEngine(String resourceArg) {
        if (!Objects.isNull(resourceArg)) {
            APIServerPath = resourceArg;
        } else {
            APIServerPath = DEFAULT_API_SERVER_PATH;
        }

        httpClient = HttpClient.newBuilder().build();
    }

    public void setUserAccessToken(String userAccessToken) {
        this.userAccessToken = userAccessToken;
    }

    public void viewFeatured() throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + userAccessToken)
                .uri(URI.create(APIServerPath + "v1/browse/featured-playlists"))
                .GET()
                .build();

        httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
        System.out.println(jsonObject.toString());
    }
}
