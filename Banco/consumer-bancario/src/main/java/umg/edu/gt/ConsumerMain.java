package umg.edu.gt;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.util.LinkedHashSet;
import java.util.Set;

public class ConsumerMain {

    private static final String COLA_RECHAZADOS = "cola_rechazados";
    private static final String COLA_ERRORES = "cola_errores";

    public static void main(String[] args) {
        String rabbitHost = "localhost";
        String rabbitUsername = "guest";
        String rabbitPassword = "guest";

        String getUrl = "https://hly784ig9d.execute-api.us-east-1.amazonaws.com/default/transacciones";
        String postUrl = "https://7e0d9ogwzd.execute-api.us-east-1.amazonaws.com/default/guardarTransacciones";

        ObjectMapper objectMapper = new ObjectMapper();
        HttpClient httpClient = HttpClient.newHttpClient();

        PostTransaccionesApiClient postApiClient =
                new PostTransaccionesApiClient(postUrl, httpClient, objectMapper);

        ConsumerService consumerService =
                new ConsumerService(postApiClient, objectMapper);

        RabbitConsumer rabbitConsumer =
                new RabbitConsumer(rabbitHost, rabbitUsername, rabbitPassword);

        TransaccionesApiClient transaccionesApiClient =
                new TransaccionesApiClient(httpClient, objectMapper, getUrl);

        Set<String> colas = new LinkedHashSet<>();

        try {
            LoteTransacciones lote = transaccionesApiClient.obtenerLote();

            if (lote != null && lote.getTransacciones() != null) {
                lote.getTransacciones().forEach(transaccion -> {
                    String banco = transaccion.getBancoDestino();

                    if (banco != null && !banco.isBlank()) {
                        colas.add(banco.trim());
                    }
                });
            }

        } catch (Exception e) {
            System.err.println("Error obteniendo bancos dinámicos desde GET: " + e.getMessage());
            System.err.println("Se usarán colas de respaldo.");
        }

        if (colas.isEmpty()) {
            colas.add("BAC");
            colas.add("BANRURAL");
            colas.add("BI");
            colas.add("GYT");
        }

        colas.add(COLA_RECHAZADOS);
        colas.add(COLA_ERRORES);

        for (String cola : colas) {
            rabbitConsumer.consumirCola(cola, consumerService);
        }

        System.out.println("Consumer iniciado correctamente.");
        System.out.println("Colas detectadas: " + colas);
        System.out.println("Escuchando colas dinámicas...");
    }
}