package com.enterprise.backend.config;
import com.enterprise.backend.service.FlagInvalidationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> featureFlagRedisTemplate(RedisConnectionFactory connectionFactory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // 1. How we save the "Key" (e.g., the flag's name)
        template.setKeySerializer(new StringRedisSerializer());

        // 2. How we save the "Value" (e.g., the JSON rules)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }
    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory connectionFactory,
            FlagInvalidationListener listener) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // We tell the container to listen to the "flag-updates" channel
        // and route any messages it hears directly to your listener class.
        container.addMessageListener(listener, new ChannelTopic("flag-updates"));

        return container;
    }
}
