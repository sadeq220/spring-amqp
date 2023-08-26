package ir.sadeqcloud.rabbitmq.rabbitmqTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpModel.AmqpPayload;
import ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpReceiver.RabbitReceiver;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
/**
 * Spring AMQP
 * org.springframework.amqp.core provide abstraction layer over AMQP model => End user code can be more portable across vendor implementations as it can be developed against the abstraction layer only
 * These abstractions are implemented by broker-specific modules, such as 'spring-rabbit' .
 * @EnableRabbit enables detection of RabbitListener annotations on any Spring-managed bean in the container
 */
@EnableRabbit
@EnableScheduling
public class RabbitmqTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqTestApplication.class, args);
	}
	@Bean
	public Queue consumerQueue(@Value("${amqp.queue.listener}") String queueName
							, @Value("${amqp.dlx}") String dlx){
		return  QueueBuilder.durable(queueName)
				.withArgument("x-dead-letter-exchange",dlx)
				.build();
	}
	@Bean
	/**
	 * turn a queue to priority queue by specifying optional argument "x-max-priority"
	 * this argument value specifies the range of message priorities that queue can prioritize.
	 * it is recommended to set this value between 1 and 5 .
	 * There is some in-memory and on-disk cost per priority level per queue.
	 * messages with larger priority numbers have larger priorities and came to head of the queue.
	 */
	public Queue priorityQueue(@Value("${amqp.priority.queue}") String priorityQueueName){
		return QueueBuilder.durable()
				.withArgument("x-max-priority",5)
				.build();
	}
	@Bean
	public FanoutExchange dlx(@Value("${amqp.dlx}") String dlx){
		return new FanoutExchange(dlx);
	}
	@Bean
	public Queue dlq(@Value("${amqp.dlq}") String dlq){
		return QueueBuilder.durable(dlq).build();
	}
	@Bean
	public Binding linkDLQToDLX(Queue dlq,FanoutExchange dlx){
		return BindingBuilder.bind(dlq).to(dlx);
	}
	@Bean
	/**
	 * The Exchange interface represents an AMQP Exchange, which is what a Message Producer sends to.
	 * Each Exchange within a virtual host of a broker has a unique name
	 * Topic exchange supports bindings with routing patterns that may include the '*' and '#' wildcards for 'exactly-one' and 'zero-or-more', respectively
	 */
	public TopicExchange createExchange(){
		return new TopicExchange("topicExchange");
	}
	@Bean
	/**
	 * the AmqpAdmin class can use Binding instances to actually trigger the binding actions on the broker
	 */
	public Binding linkQueueToExchange(TopicExchange topicExchange,Queue consumerQueue){
	return BindingBuilder.bind(consumerQueue).to(topicExchange).with("test.*");
	}
	@Bean
	public Binding linkPriorityQueueToExchange(TopicExchange topicExchange,Queue priorityQueue){
		return BindingBuilder.bind(priorityQueue).to(topicExchange).with("priority.*");
	}

	/**
	 * All protocols supported by RabbitMQ are TCP-based and assume long-lived connections (a new connection is not opened per protocol operation)
	 * Since connections are meant to be long-lived, clients usually consume messages by registering a subscription and having messages delivered (pushed) to them instead of polling
	 *
	 */
	@Bean
	public ConnectionFactory connectionAndChannelsToRabbitmqMessageBroker(){
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
		/**
		 * A ConnectionFactory implementation that (when the cache mode is CachingConnectionFactory.CacheMode.CHANNEL (default) returns the same Connection from all createConnection() calls,
		 * and ignores calls to Connection.close() and caches Channel.
		 * CachingConnectionFactory, by default, establishes a single connection proxy that can be shared by the application.
		 * Sharing of the connection is possible since the “unit of work” for messaging with AMQP is actually a “channel”
		 *
		 * default cache size is one
		 */
		cachingConnectionFactory.setHost("localhost");
		cachingConnectionFactory.setPort(5672);
		cachingConnectionFactory.setUsername("guest");
		cachingConnectionFactory.setPassword("guest");
		cachingConnectionFactory.setConnectionNameStrategy(connectionFactory->"connection-name");
		return cachingConnectionFactory;
	}
	@Bean
	/**
	 * use reflection API to invoke Message-driven POJO
	 * decouple the business from messaging system
	 * You can subclass the adapter and provide an implementation of getListenerMethodName() to dynamically select different methods based on the message.
	 * This method has two parameters, originalMessage and extractedMessage, the latter being the result of any conversion.
	 * buildListenerArguments(Object, Channel, Message) method helps listener to get Channel and Message arguments to do more, such as calling channel.basicReject(long, boolean) in manual acknowledge mode.
	 */
	public MessageListenerAdapter channelAdapter(RabbitReceiver rabbitReceiver,MessageConverter messageConverter){
		MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(rabbitReceiver, "onMessageArriaval");
		messageListenerAdapter.setMessageConverter(messageConverter);

		return messageListenerAdapter;
	}

	/**
	 * retry-backoff-deadLettering
	 * DLX (Dead Letter Exchanges) and DLQ (Dead-Letter-Queues) are both something you can configure as a policy for all queues or manually per queue.
	 * Dead-Lettering defines what should happen with messages that get rejected by a consumer ( basic.reject or basic.nack with requeue parameter set to false)
	 * we want to first retry a failed messages (Poison Messages are messages that can not get consumed) and then deadLetter them
	 * RejectAndDontRequeueRecoverer : MessageRecover that causes the listener container to reject the message without requeuing. This enables failed messages to be sent to a Dead Letter Exchange/Queue, if the broker is so configured.
	 * read more about <a href="https://www.rabbitmq.com/dlx.html">DLX</a>
	 */
	@Bean
	public RetryOperationsInterceptor amqpRetryBackoff(){
		return RetryInterceptorBuilder.stateless()
				.backOffOptions(100,3.0,1000) // 100ms wait time (delay) to retry
				.maxAttempts(3)
				.recoverer(new RejectAndDontRequeueRecoverer()) // Callback for message that was consumed but failed all retry attempts.
				.build();
	}
	@Bean
	/**
	 * define messageListenerContainer
	 */
	public SimpleMessageListenerContainer createMessageListenerContainer(ConnectionFactory connectionFactory
																		,@Value("${amqp.queue.listener}") String queueName
																		,MessageListenerAdapter messageListenerAdapter
																		,RetryOperationsInterceptor retryOperationsInterceptor){
		SimpleMessageListenerContainer messageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
		messageListenerContainer.addQueueNames(queueName);
		messageListenerContainer.setMessageListener(messageListenerAdapter);
		messageListenerContainer.setAdviceChain(retryOperationsInterceptor);
		return messageListenerContainer;
	}

	/**
	 * Provides synchronous send and receive methods
	 *  delegate to an instance of
	 *  {@link org.springframework.amqp.support.converter.MessageConverter} to perform conversion
	 *  to and from AMQP byte[] payload type.
	 *
	 *  Also supports basic RPC(request/reply) pattern (send to exchange and expect result form queue) e.g.
	 *        Message sendAndReceive(String routingKey, Message message) throws AmqpException;
	 * 		  amqpTemplate sets reply-to header to an exclusive queue
	 */
	@Bean
	public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,MessageConverter messageConverter){
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
		rabbitTemplate.setExchange("topicExchange");
		rabbitTemplate.setRoutingKey("test.template");
		rabbitTemplate.setMessageConverter(messageConverter);
		return rabbitTemplate;
	}
	@Bean
	public ObjectMapper jsonConverter(){
		return new ObjectMapper();
	}
	@Bean
	public MessageConverter messageConverter(ObjectMapper objectMapper
											,@Qualifier("customized-classMapper") ClassMapper classMapper
											,Jackson2JavaTypeMapper javaTypeMapper){
		Jackson2JsonMessageConverter jackson2JsonMessageConverter = new Jackson2JsonMessageConverter(objectMapper);
		jackson2JsonMessageConverter.setClassMapper(classMapper);
		//jackson2JsonMessageConverter.setJavaTypeMapper(javaTypeMapper);
		return jackson2JsonMessageConverter;
	}
	@Bean
	/**
	 * this is why to we use MessageConverter insteadOf ObjectMapper
	 *  byte[](content-encoding) => string -> messageConverter("__TypeId__",content-type) => java obj
	 */
	public Jackson2JavaTypeMapper javaTypeMapper(){
		DefaultJackson2JavaTypeMapper javaTypeMapper = new DefaultJackson2JavaTypeMapper();
		Map<String,Class<?>> idClassMapping=new HashMap<>();
		idClassMapping.put("amqpPayload",AmqpPayload.class);
		javaTypeMapper.setIdClassMapping(idClassMapping);
		javaTypeMapper.addTrustedPackages("ir.sadeqcloud.rabbitmq.rabbitmqTest.model.amqpModel");
		return javaTypeMapper;
	}
	@Bean(name = "customized-classMapper")
	public ClassMapper messageConverterClassMapper(){
		DefaultClassMapper classMapper = new DefaultClassMapper();
		Map<String,Class<?>> idClassMapping=new HashMap<>();
		idClassMapping.put("amqpPayload",AmqpPayload.class);
		classMapper.setIdClassMapping(idClassMapping);
		return classMapper;
	}
}
