package umg.edu.gt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ConsumerService {

    private final PostTransaccionesApiClient postApiClient;
    private final ObjectMapper objectMapper;

    public ConsumerService(PostTransaccionesApiClient postApiClient, ObjectMapper objectMapper) {
        this.postApiClient = postApiClient;
        this.objectMapper = objectMapper;
    }

    public void procesarMensaje(String queueName, Delivery delivery, Channel channel) {
        long deliveryTag = delivery.getEnvelope().getDeliveryTag();
        String json = new String(delivery.getBody(), StandardCharsets.UTF_8);

        try {
            System.out.println("Mensaje recibido desde la cola " + queueName + ": " + json);

            Transaccion transaccion = objectMapper.readValue(json, Transaccion.class);

            transaccion.setNombre("Eduardo Gabriel Visoni Morales");
            transaccion.setCarnet("0905-22-1146");

            String nuevoId = transaccion.getIdTransaccion() + "-" + UUID.randomUUID();
            transaccion.setIdTransaccion(nuevoId);

            boolean enviadoCorrectamente = postApiClient.enviarTransaccion(transaccion);

            if (enviadoCorrectamente) {
                channel.basicAck(deliveryTag, false);
                System.out.println("ACK enviado para transacción: " + transaccion.getIdTransaccion());
                System.out.println("POST exitoso. Transacción procesada correctamente.");
            } else {
                System.err.println("POST fallido para transacción: " + transaccion.getIdTransaccion());
                System.err.println("NO se envía ACK. Se hará NACK con reencolado.");

                channel.basicNack(deliveryTag, false, true);
                System.err.println("Mensaje reenviado a la cola para reintento.");
            }

        } catch (Exception e) {
            System.err.println("Error procesando mensaje de la cola " + queueName + ": " + e.getMessage());
            System.err.println("Se hará NACK con reencolado.");

            try {
                channel.basicNack(deliveryTag, false, true);
                System.err.println("Mensaje reenviado a la cola por error.");
            } catch (IOException ioException) {
                System.err.println("Error al hacer NACK del mensaje: " + ioException.getMessage());
            }
        }
    }
}