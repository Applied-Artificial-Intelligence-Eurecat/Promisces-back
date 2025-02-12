package org.eurecat.promisces.substances;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

@Service
public class SimilarityService {

    public List<SimilarityReturn> getSimilarities(String substance_smiles, String substance_name) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        String requestString = "{\"smiles\": \"" + substance_smiles + "\",\"substance_name\":\"" + substance_name + "\"}";

        System.out.println(requestString);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://promiscessimilarity:5000/comparation"))
                .method("POST", HttpRequest.BodyPublishers.ofString(requestString))
                .timeout(Duration.ofSeconds(300))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        try {
            // Parse the input string to a list of lists using Jackson
            ObjectMapper mapper = new ObjectMapper();
            List<List<Object>> parsedList = mapper.readValue(response.body(), List.class);

            // Convert to List<MyObject>
            List<SimilarityReturn> myObjects = new LinkedList<>();
            for (List<Object> innerList : parsedList) {
                SimilarityReturn myObject = SimilarityReturn.builder()
                        .name((String) innerList.get(0))
                        .likeness(Float.parseFloat(String.valueOf(innerList.get(1))))
                        .p(Float.parseFloat(String.valueOf(innerList.get(2))))
                        .m(Float.parseFloat(String.valueOf(innerList.get(3))))
                        .t(Float.parseFloat(String.valueOf(innerList.get(4))))
                        .k(Float.parseFloat(String.valueOf(innerList.get(5)))).build();
                myObjects.add(myObject);
            }

            return myObjects;
        } catch (Exception e) {
            e.printStackTrace();
            return new LinkedList<>();
        }
    }
}
