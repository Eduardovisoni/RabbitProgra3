package umg.edu.gt;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitPublisher {

    private final ConnectionFactory factory;

    public RabbitPublisher(ConnectionFactory factory) {
        this.factory = factory;
    }

    public void publicarJsonEnCola(String queueName, String json) throws IOException, TimeoutException {
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // Cola durable: RabbitMQ la conserva
            channel.queueDeclare(queueName, true, false, false, null);

            channel.basicPublish(
                    "",              // default exchange
                    queueName,        // routing key = nombre de cola
                    null,
                    json.getBytes(StandardCharsets.UTF_8)
            );
        }
    }
}