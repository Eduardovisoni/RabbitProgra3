package umg.edu.gt;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PostTransaccionesApiClient {

    private final String postUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PostTransaccionesApiClient(String postUrl, HttpClient httpClient, ObjectMapper objectMapper) {
        this.postUrl = postUrl;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public boolean enviarTransaccion(Transaccion transaccion) {
        try {
            String json = objectMapper.writeValueAsString(transaccion);
            System.out.println("JSON enviado al POST: " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(postUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("POST enviado para transacción: " + transaccion.getIdTransaccion());
            System.out.println("Código de respuesta: " + response.statusCode());
            System.out.println("Respuesta del servidor: " + response.body());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                System.out.println("POST exitoso. Respuesta " + response.statusCode() + " = transacción procesada correctamente.");
                return true;
            }

            System.err.println("POST fallido. Código recibido: " + response.statusCode());
            return false;

        } catch (IOException | InterruptedException e) {
            System.err.println("Error enviando transacción al POST: " + e.getMessage());
            return false;
        }
    }
}