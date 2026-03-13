package umg.edu.gt;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

public class RabbitConnectionTest {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            String queueName = "TEST_BANCO";
            channel.queueDeclare(queueName, true, false, false, null);

            String messageJson = """
                    {
                      "idTransaccion":"TX-TEST-1",
                      "monto":123.45,
                      "moneda":"GTQ",
                      "cuentaOrigen":"001-TEST",
                      "bancoDestino":"TEST_BANCO"
                    }
                    """;

            channel.basicPublish("", queueName, null, messageJson.getBytes(StandardCharsets.UTF_8));

            System.out.println("✅ Mensaje publicado en cola: " + queueName);
        }
    }
}