package umg.edu.gt;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitConsumer {

    private final String host;
    private final String username;
    private final String password;

    public RabbitConsumer(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public void consumirCola(String queueName, ConsumerService consumerService) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);

        try {
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            channel.queueDeclare(queueName, true, false, false, null);

            System.out.println("Escuchando cola: " + queueName);

            boolean autoAck = false;

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                consumerService.procesarMensaje(queueName, delivery, channel);
            };

            channel.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> {
                System.out.println("Consumer cancelado para la cola: " + queueName);
            });

        } catch (IOException | TimeoutException e) {
            System.err.println("Error al consumir la cola " + queueName + ": " + e.getMessage());
        }
    }
}