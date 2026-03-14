package umg.edu.gt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ConsumerService {

    private static final String COLA_RECHAZADOS = "cola_rechazados";
    private static final String COLA_ERRORES = "cola_errores";
    private static final double MONTO_MAXIMO = 4000.00;

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
            Transaccion transaccion = objectMapper.readValue(json, Transaccion.class);

            transaccion.setNombre("Eduardo Gabriel Visoni Morales");
            transaccion.setCarnet("0905-22-1146");

            if (transaccion.getIdTransaccion() != null && !transaccion.getIdTransaccion().contains("-")) {
                transaccion.setIdTransaccion(transaccion.getIdTransaccion() + "-" + UUID.randomUUID());
            }

            String estado = transaccion.getMonto() > MONTO_MAXIMO ? "RECHAZADA" : "ACEPTADA";

            System.out.println("Cola atendida: " + queueName);
            System.out.println("ID procesado: " + transaccion.getIdTransaccion());
            System.out.println("Monto: Q." + transaccion.getMonto());
            System.out.println("Estado: " + estado);

            if (COLA_RECHAZADOS.equals(queueName)) {
                System.out.println("La transacción ya está en cola_rechazados. No se enviará al POST.");
                channel.basicAck(deliveryTag, false);
                System.out.println("ACK enviado para transacción rechazada: " + transaccion.getIdTransaccion());
                return;
            }

            if (COLA_ERRORES.equals(queueName)) {
                System.out.println("La transacción ya está en cola_errores. Se conserva para evidencia y no se reprocesará.");
                channel.basicAck(deliveryTag, false);
                System.out.println("ACK enviado para transacción en cola_errores: " + transaccion.getIdTransaccion());
                return;
            }

            if (transaccion.getMonto() > MONTO_MAXIMO) {
                enviarACola(channel, COLA_RECHAZADOS, transaccion);
                channel.basicAck(deliveryTag, false);
                System.out.println("Transacción enviada a cola_rechazados: " + transaccion.getIdTransaccion());
                System.out.println("ACK enviado para transacción rechazada: " + transaccion.getIdTransaccion());
                return;
            }

            boolean enviadoCorrectamente = postApiClient.enviarTransaccion(transaccion);

            if (enviadoCorrectamente) {
                channel.basicAck(deliveryTag, false);
                System.out.println("ACK enviado para transacción: " + transaccion.getIdTransaccion());
                System.out.println("POST exitoso. Transacción procesada correctamente.");
            } else {
                System.err.println("POST fallido para transacción: " + transaccion.getIdTransaccion());
                enviarACola(channel, COLA_ERRORES, transaccion);
                channel.basicAck(deliveryTag, false);
                System.err.println("Transacción enviada a cola_errores: " + transaccion.getIdTransaccion());
                System.err.println("ACK enviado del mensaje original después de moverlo a cola_errores.");
            }

        } catch (Exception e) {
            System.err.println("Error procesando mensaje de la cola " + queueName + ": " + e.getMessage());

            try {
                channel.queueDeclare(COLA_ERRORES, true, false, false, null);
                channel.basicPublish(
                        "",
                        COLA_ERRORES,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        json.getBytes(StandardCharsets.UTF_8)
                );

                channel.basicAck(deliveryTag, false);
                System.err.println("Mensaje enviado a cola_errores.");
                System.err.println("ACK enviado del mensaje original tras moverlo a cola_errores.");

            } catch (IOException ioException) {
                System.err.println("Error al enviar a cola_errores: " + ioException.getMessage());

                try {
                    channel.basicNack(deliveryTag, false, true);
                    System.err.println("Se hizo NACK con reencolado porque no pudo enviarse a cola_errores.");
                } catch (IOException nackException) {
                    System.err.println("Error al hacer NACK del mensaje: " + nackException.getMessage());
                }
            }
        }
    }

    private void enviarACola(Channel channel, String queueName, Transaccion transaccion) throws IOException {
        String json = objectMapper.writeValueAsString(transaccion);

        channel.queueDeclare(queueName, true, false, false, null);
        channel.basicPublish(
                "",
                queueName,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                json.getBytes(StandardCharsets.UTF_8)
        );
    }
}