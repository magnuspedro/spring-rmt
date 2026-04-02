package br.com.magnus.detectionandrefactoring.integration;

import com.github.sonus21.rqueue.core.RqueueEndpointManager;
import com.github.sonus21.rqueue.core.RqueueMessageEnqueuer;
import com.github.sonus21.rqueue.listener.QueueDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.SimpleMessageConverter;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Proxy;

@TestConfiguration
public class TestRedisConfiguration {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory(
            @Value("${spring.data.redis.host}") String host,
            @Value("${spring.data.redis.port}") int port) {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }

    @Bean
    @Primary
    public RqueueEndpointManager rqueueEndpointManager() {
        return new RqueueEndpointManager() {
            private final Set<String> queues = ConcurrentHashMap.newKeySet();

            @Override
            public void registerQueue(String queueName, String... deadLetterQueueNames) {
                queues.add(queueName);
            }

            @Override
            public boolean isQueueRegistered(String queueName) {
                return queues.contains(queueName);
            }

            @Override
            public List<QueueDetail> getQueueConfig(String queueName) {
                return Collections.emptyList();
            }

            @Override
            public boolean pauseUnpauseQueue(String queueName, boolean pause) {
                return false;
            }

            @Override
            public boolean pauseUnpauseQueue(String queueName, String deadLetterQueueName, boolean pause) {
                return false;
            }

            @Override
            public boolean isQueuePaused(String queueName) {
                return false;
            }

            @Override
            public boolean isQueuePaused(String queueName, String deadLetterQueueName) {
                return false;
            }
        };
    }

    @Bean
    @Primary
    public RqueueMessageEnqueuer rqueueMessageEnqueuer(RedisTemplate<String, Object> redisTemplate) {
        MessageConverter messageConverter = new SimpleMessageConverter();
        return (RqueueMessageEnqueuer) Proxy.newProxyInstance(
                RqueueMessageEnqueuer.class.getClassLoader(),
                new Class<?>[]{RqueueMessageEnqueuer.class},
                (proxy, method, args) -> {
                    return switch (method.getName()) {
                        case "enqueue" -> {
                            var queueName = (String) args[0];
                            var payload = args[args.length - 1];
                            redisTemplate.opsForList().rightPush("rqueue-pattern:" + queueName, String.valueOf(payload));
                            yield method.getReturnType() == boolean.class
                                    ? Boolean.TRUE
                                    : "rqueue-pattern:" + queueName;
                        }
                        case "getMessageConverter" -> messageConverter;
                        case "toString" -> "TestRqueueMessageEnqueuer";
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "equals" -> proxy == args[0];
                        default -> throw new UnsupportedOperationException(method.getName());
                    };
                }
        );
    }
}
