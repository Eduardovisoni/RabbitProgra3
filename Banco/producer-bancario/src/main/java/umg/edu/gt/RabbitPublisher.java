package umg.edu.gt;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

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

            channel.queueDeclare(queueName, true, false, false, null);

            channel.basicPublish(
                    "",
                    queueName,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    json.getBytes(StandardCharsets.UTF_8)
            );

            System.out.println("Mensaje publicado en cola: " + queueName);
        }
    }
}