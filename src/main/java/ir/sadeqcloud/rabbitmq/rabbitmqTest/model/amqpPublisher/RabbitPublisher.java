package ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpPublisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpModel.AmqpPayload;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class RabbitPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    @Autowired
    public RabbitPublisher(RabbitTemplate rabbitTemplate,
                           ObjectMapper objectMapper){
        this.rabbitTemplate=rabbitTemplate;
        this.objectMapper=objectMapper;
    }

    public void publishSome(AmqpPayload amqpPayload){
        /**
         * 	 Convert a Java object to an Amqp {@link Message} and send it to a default exchange
         * 	 with a default routing key.
         */
        rabbitTemplate.convertAndSend(amqpPayload);
    }

    @Scheduled(fixedDelay = 20_000l)
    public void publishMessage() throws JsonProcessingException {
        long randomId = (long) (Math.random()*100);
        AmqpPayload amqpPayload = new AmqpPayload("sadeq", randomId, "hi there!", Instant.now());
        Message amqpMessage = MessageBuilder.withBody(objectMapper.writeValueAsBytes(amqpPayload))
                                            .setContentType("application/json")//use by MessageConverter on consumer
                                            .setHeader("__TypeId__","amqpPayload")//use by MessageConverter on consumer
                                            .setContentEncoding("UTF-8")//use by MessageConverter on consumer
                                            .build();
        this.publishSome(amqpPayload);
    }
}
