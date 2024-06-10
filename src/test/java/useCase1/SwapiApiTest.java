package useCase1;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class SwapiApiTest {

    private static final String BASE_URL = "https://swapi.dev/api";

    @Test
    public void findFilmWithLatestReleaseDate() throws IOException, ParseException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse response = client.execute(new HttpGet(BASE_URL + "/films/"));

        Assert.assertEquals(response.getStatusLine().getStatusCode(), 200);

        JsonArray films = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject().getAsJsonArray("results");

        String latestFilmTitle = "";
        Date latestReleaseDate = new Date(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (JsonElement filmElement : films) {
            JsonObject film = filmElement.getAsJsonObject();
            Date releaseDate = dateFormat.parse(film.get("release_date").getAsString());

            if (releaseDate.after(latestReleaseDate)) {
                latestReleaseDate = releaseDate;
                latestFilmTitle = film.get("title").getAsString();
            }
        }

        client.close();

        Reporter.log("The film with the latest release date is: " + latestFilmTitle + " (" + dateFormat.format(latestReleaseDate) + ")");
    }

    @Test
    public void findTallestCharacterInLatestFilm() throws IOException, ParseException {
        CloseableHttpClient client = HttpClients.createDefault();
        String latestFilmUrl = getLatestFilmUrl(client);

        JsonArray characters = getCharactersFromFilm(client, latestFilmUrl);

        int tallestHeight = 0;
        String tallestCharacterName = "";

        for (JsonElement characterElement : characters) {
            String characterUrl = characterElement.getAsString();
            HttpResponse response = client.execute(new HttpGet(characterUrl));
            JsonObject character = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject();
            String heightStr = character.get("height").getAsString();

            if (!heightStr.equals("unknown")) {
                int height = Integer.parseInt(heightStr);
                if (height > tallestHeight) {
                    tallestHeight = height;
                    tallestCharacterName = character.get("name").getAsString();
                }
            }
        }

        client.close();

        Reporter.log("The tallest character in the film with the latest release date is: " + tallestCharacterName);
    }

    @Test
    public void findTallestCharacterInAllFilms() throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        Set<String> characterUrls = new HashSet<>();

        HttpResponse response = client.execute(new HttpGet(BASE_URL + "/films/"));
        JsonArray films = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject().getAsJsonArray("results");

        for (JsonElement filmElement : films) {
            JsonArray characters = filmElement.getAsJsonObject().getAsJsonArray("characters");
            for (JsonElement characterElement : characters) {
                characterUrls.add(characterElement.getAsString());
            }
        }

        int tallestHeight = 0;
        String tallestCharacterName = "";

        for (String characterUrl : characterUrls) {
            HttpResponse characterResponse = client.execute(new HttpGet(characterUrl));
            JsonObject character = JsonParser.parseString(EntityUtils.toString(characterResponse.getEntity())).getAsJsonObject();
            String heightStr = character.get("height").getAsString();

            if (!heightStr.equals("unknown")) {
                int height = Integer.parseInt(heightStr);
                if (height > tallestHeight) {
                    tallestHeight = height;
                    tallestCharacterName = character.get("name").getAsString();
                }
            }
        }

        client.close();

        Reporter.log("The tallest character in all Star Wars films is: " + tallestCharacterName);
    }

    private String getLatestFilmUrl(CloseableHttpClient client) throws IOException, ParseException {
        HttpResponse response = client.execute(new HttpGet(BASE_URL + "/films/"));
        JsonArray films = JsonParser.parseString(EntityUtils.toString(response.getEntity())).getAsJsonObject().getAsJsonArray("results");

        String latestFilmUrl = "";
        Date latestReleaseDate = new Date(0);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (JsonElement filmElement : films) {
            JsonObject film = filmElement.getAsJsonObject();
            Date releaseDate = dateFormat.parse(film.get("release_date").getAsString());

            if (releaseDate.after(latestReleaseDate)) {
                latestReleaseDate = releaseDate;
                latestFilmUrl = film.get("url").getAsString();
            }
        }

        return latestFilmUrl;
    }

    private JsonArray getCharactersFromFilm(CloseableHttpClient client, String filmUrl) throws IOException {
        HttpResponse response = client.execute(new HttpGet(filmUrl));
        return JsonParser.parseString(EntityUtils.toString(response.getEntity()))
                .getAsJsonObject().getAsJsonArray("characters");
    }
}