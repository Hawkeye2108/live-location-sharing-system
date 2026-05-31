package com.org.livelocationsharing.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 * Configures Redis:
 *  - JSON serialization for values (human-readable in Redis CLI)
 *  - String serialization for keys
 *  - Keyspace notification listener for TTL expiry events
 *
 * Requires redis.conf:  notify-keyspace-events Ex
 */
@Slf4j
@Configuration
public class RedisConfig {

    /**
     * RedisTemplate with String keys and JSON values.
     * This lets you inspect keys with redis-cli and read them as JSON.
     */
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(factory);
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
//        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
//        template.afterPropertiesSet();
//        log.info("RedisTemplate configured with JSON value serializer");
//        return template;
//    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Build an ObjectMapper with Java 8 time support (Instant, etc.)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // Store type info so deserialization knows the concrete class
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );

        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        template.afterPropertiesSet();

        log.info("RedisTemplate configured with Jackson2JsonRedisSerializer");
        return template;
    }

    /**
     * Listens for __keyevent@*__:expired events from Redis.
     * Fires when any key with a TTL expires — used to detect session inactivity.
     */
    @Bean
    public RedisMessageListenerContainer keyExpiryListenerContainer(
            RedisConnectionFactory factory,
            MessageListenerAdapter expiryListenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(
                expiryListenerAdapter,
                new PatternTopic("__keyevent@*__:expired")
        );
        log.info("Redis keyspace expiry listener container registered");
        return container;
    }

    /**
     * Adapts the raw Redis message to SessionExpiryScheduler.onKeyExpired(String).
     */
    @Bean
    public MessageListenerAdapter expiryListenerAdapter(
            com.org.livelocationsharing.scheduler.SessionExpiryScheduler scheduler) {
        return new MessageListenerAdapter(scheduler, "onKeyExpired");
    }
}