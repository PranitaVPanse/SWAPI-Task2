package useCase2;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

public class SwapiContractTest {

    private static final String SWAPI_BASE_URL = "https://swapi.dev/api";

    @Test
    public void testPeopleApiResponse() {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // Send GET request to /people endpoint
            HttpGet request = new HttpGet(SWAPI_BASE_URL + "/people");
            String response = EntityUtils.toString(httpClient.execute(request).getEntity());

            // Validate response content
            Assert.assertNotNull(response, "API response is null");
            Assert.assertFalse(response.isEmpty(), "API response is empty");
            
            Reporter.log("People Api Response:" + response);

            // Add more validation as needed (e.g., JSON parsing, field checks)
        } catch (Exception e) {
            Assert.fail("Error during API request: " + e.getMessage());
        }
    }
}
