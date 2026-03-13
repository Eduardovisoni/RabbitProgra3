package umg.edu.gt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.ConnectionFactory;

import java.net.http.HttpClient;

public class ProducerMain {
    public static void main(String[] args) throws Exception {
        String urlGet = "https://hly784ig9d.execute-api.us-east-1.amazonaws.com/default/transacciones";

        // HTTP
        HttpClient httpClient = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        TransaccionesApiClient apiClient = new TransaccionesApiClient(httpClient, objectMapper, urlGet);

        // RabbitMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        RabbitPublisher publisher = new RabbitPublisher(factory);

        // Orquestación
        ProducerService service = new ProducerService(apiClient, publisher, objectMapper);
        service.ejecutar();
    }
}