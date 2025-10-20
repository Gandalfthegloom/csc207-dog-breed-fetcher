package dogapi;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

/**
 * BreedFetcher implementation that relies on the dog.ceo API.
 * Note that all failures get reported as BreedNotFoundException
 * exceptions to align with the requirements of the BreedFetcher interface.
 */
public class DogApiBreedFetcher implements BreedFetcher {
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Fetch the list of sub breeds for the given breed from the dog.ceo API.
     *
     * @param breed the breed to fetch sub breeds for
     * @return list of sub breeds for the given breed
     * @throws BreedNotFoundException if the breed does not exist (or if the API call fails for any reason)
     */
    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        String urlMain = "https://dog.ceo/api/breed/" + breed + "/list";

        Request request = new Request.Builder().url(urlMain).build();

        try (Response response = client.newCall(request).execute()) {
            // 1) HTTP-level errors -> BreedNotFoundException
            if (!response.isSuccessful()) {
                throw new BreedNotFoundException("HTTP " + response.code() + " for breed '" + breed + "'");
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new BreedNotFoundException("Empty body for breed '" + breed + "'");
            }

            String json = body.string(); // note: can be called only once
            JSONObject obj = new JSONObject(json);

            // 2) API-level status check
            String status = obj.optString("status", "");
            if (!"success".equalsIgnoreCase(status)) {
                // Dog CEO tends to return {"status":"error","message":"..."} on unknown breeds
                String apiMsg = obj.optString("message", "Unknown API error");
                throw new BreedNotFoundException("API error for '" + breed + "': " + apiMsg);
            }

            // 3) Happy path: parse the array
            JSONArray arr = obj.getJSONArray("message");
            List<String> subBreeds = new ArrayList<>(arr.length());
            for (int i = 0; i < arr.length(); i++) {
                subBreeds.add(arr.getString(i));
            }
            return subBreeds;

        } catch (IOException e) {
            // Network/IO -> wrap
            throw new BreedNotFoundException("IO error for '" + breed + "': " + e.getMessage());
        } catch (org.json.JSONException e) {
            // Unexpected/malformed JSON -> wrap
            throw new BreedNotFoundException("JSON error for '" + breed + "': " + e.getMessage());
        }
    }

}