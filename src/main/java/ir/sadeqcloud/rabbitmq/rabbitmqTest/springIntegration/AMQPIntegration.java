package ir.sadeqcloud.rabbitmq.rabbitmqTest.springIntegration;

import ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpModel.AmqpPayload;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.channel.interceptor.MessageSelectingInterceptor;
import org.springframework.integration.selector.PayloadTypeSelector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.ChannelInterceptor;

import java.util.concurrent.Executors;

@Configuration
public class AMQPIntegration {
    @Bean
    public MessageChannel pollableChannel(){
        return new QueueChannel(5);
    }
    @Bean
    /**
     * define messageListenerContainer
     */
    public SimpleMessageListenerContainer integrationMessageListenerContainer(ConnectionFactory connectionFactory
            , @Value("${amqp.queue.listener}") String queueName){
        SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
        messageListenerContainer.addQueueNames(queueName);
        return messageListenerContainer;
    }
    @Bean
    public AmqpInboundChannelAdapter inboundChannelAdapter(@Qualifier("integrationMessageListenerContainer") MessageListenerContainer messageListenerContainer,
                                                           MessageConverter messageConverter){
        AmqpInboundChannelAdapter amqpInboundChannelAdapter = new AmqpInboundChannelAdapter(messageListenerContainer);
        amqpInboundChannelAdapter.setMessageConverter(messageConverter);
        amqpInboundChannelAdapter.setOutputChannel(pollableChannel());
        return amqpInboundChannelAdapter;
    }

    @ServiceActivator(inputChannel = "pollableChannel",poller = @Poller(fixedRate = "10000"))
    /**
     * An outbound-channel-adapter element (a @ServiceActivator for Java configuration)
     * can also connect a MessageChannel to any POJO consumer method that should be invoked with the payload of messages sent to that channel
     */
    public void processMessage(Message<AmqpPayload> amqpMessage){
    System.out.println("message arrived");
    }

    @Bean
    public SubscribableChannel eventMessageChannel(ChannelInterceptor channelInterceptor){
        PublishSubscribeChannel publishSubscribeChannel = new PublishSubscribeChannel(Executors.newFixedThreadPool(10));//MessageDispatcher thread pool
        publishSubscribeChannel.setDatatypes(String.class);//DataType channel enterprise integration pattern
        publishSubscribeChannel.addInterceptor(channelInterceptor);
        return publishSubscribeChannel;
    }

    /**
     * DataType channel enterprise integration pattern
     * @return ChannelInterceptor to filter messages
     */
    @Bean
    public MessageSelectingInterceptor dataTypeChannel(){
        PayloadTypeSelector payloadTypeSelector = new PayloadTypeSelector(AmqpPayload.class);
        MessageSelectingInterceptor messageSelectingInterceptor = new MessageSelectingInterceptor(payloadTypeSelector);
        return messageSelectingInterceptor;
    }
}
