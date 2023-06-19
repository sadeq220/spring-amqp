package ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpPublisher;

import ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpModel.AmqpPayload;
import org.springframework.amqp.rabbit.core.RabbitOperations;

import java.util.List;

/**
 * Callback for using the same channel for multiple RabbitTemplate operations.
 */
public class RabbitScopedOperations implements RabbitOperations.OperationsCallback<Void> {
    private final List<AmqpPayload> messages;

    public RabbitScopedOperations(List<AmqpPayload> messages) {
        this.messages = messages;
    }
    @Override
    public Void doInRabbit(RabbitOperations operations) {
        messages.forEach(message-> operations.convertAndSend(message));
        return null;
    }
}
