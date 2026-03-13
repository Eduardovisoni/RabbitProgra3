package umg.edu.gt;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TransaccionesApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String endpointUrl;

    public TransaccionesApiClient(HttpClient httpClient, ObjectMapper objectMapper, String endpointUrl) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.endpointUrl = endpointUrl;
    }

    public LoteTransacciones obtenerLote() throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Error al obtener transacciones. Status: "
                    + response.statusCode() + " Body: " + response.body());
        }

        return objectMapper.readValue(response.body(), LoteTransacciones.class);
    }
}