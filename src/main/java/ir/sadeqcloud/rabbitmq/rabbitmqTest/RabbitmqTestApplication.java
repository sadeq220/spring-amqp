package ir.sadeqcloud.rabbitmq.rabbitmqTest;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
/**
 * Spring AMQP
 * org.springframework.amqp.core provide abstraction layer over AMQP model => End user code can be more portable across vendor implementations as it can be developed against the abstraction layer only
 * These abstractions are implemented by broker-specific modules, such as 'spring-rabbit' .
 * @EnableRabbit enables detection of RabbitListener annotations on any Spring-managed bean in the container
 */
@EnableRabbit
public class RabbitmqTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(RabbitmqTestApplication.class, args);
	}
	@Bean
	public Queue createQueue(){
		return new Queue("testingQueue",true);
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
	public Binding linkQueueToExchange(TopicExchange topicExchange,Queue queue){
	return BindingBuilder.bind(queue).to(topicExchange).with("test.*");
	}
	@Bean
	public ConnectionFactory connectionAndChannelsToRabbitmqMessageBroker(){
		CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
		/**
		 * A ConnectionFactory implementation that (when the cache mode is CachingConnectionFactory.CacheMode.CHANNEL (default) returns the same Connection from all createConnection() calls,
		 * and ignores calls to Connection.close() and caches Channel.
		 */
		cachingConnectionFactory.setHost("localhost");
		cachingConnectionFactory.setPort(5672);
		cachingConnectionFactory.setUsername("guest");
		cachingConnectionFactory.setPassword("guest");
		return cachingConnectionFactory;
	}
	@Bean
	public SimpleMessageListenerContainer createMessageListenerContainer(ConnectionFactory connectionFactory){
		SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer(connectionFactory);
		return simpleMessageListenerContainer;
	}

}
