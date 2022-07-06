package ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpReceiver;

import ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpModel.AmqpPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
/**
 * Message-driven POJO
 */
public class RabbitReceiver {
    private static final Logger LOGGER= LoggerFactory.getLogger(RabbitReceiver.class);
    public void onMessageArriaval(AmqpPayload amqpPayload){
    LOGGER.info("successful listen");
    }
}
