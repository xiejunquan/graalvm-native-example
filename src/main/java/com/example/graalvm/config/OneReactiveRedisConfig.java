package com.example.graalvm.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.protocol.ProtocolVersion;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.RedisConnectionDetails;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author xiejunquan
 * @create 2024/7/24 11:10
 */
@Configuration
public class OneReactiveRedisConfig extends AbstractLettuceConnectionConfig {

    @Primary
    @Bean("oneRedisProperties")
    @ConfigurationProperties(prefix = "spring.redis.one")
    @Override
    public RedisProperties getProperties() {
        return new RedisProperties();
    }

    @Primary
    @Bean("oneReactiveRedisTemplate")
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate() {
        LettuceConnectionFactory lettuceConnectionFactory = createConnectionFactory((builder) -> {
            builder.readFrom(ReadFrom.MASTER);
            builder.clientOptions(ClientOptions.builder().protocolVersion(ProtocolVersion.RESP2).build());
        });
        lettuceConnectionFactory.start();
        return new ReactiveStringRedisTemplate(lettuceConnectionFactory);
    }


}
