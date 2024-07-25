package com.example.graalvm.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.protocol.ProtocolVersion;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;

/**
 * @author xiejunquan
 * @create 2024/7/24 11:10
 */
@Configuration
public class TwoReactiveRedisConfig extends AbstractLettuceConnectionConfig {

    @Bean("twoRedisProperties")
    @ConfigurationProperties(prefix = "spring.redis.two")
    @Override
    public RedisProperties getProperties() {
        return new RedisProperties();
    }

    @Bean("twoReactiveRedisTemplate")
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate() {
        LettuceConnectionFactory lettuceConnectionFactory = createConnectionFactory((builder) -> {
            builder.readFrom(ReadFrom.MASTER);
            builder.clientOptions(ClientOptions.builder().protocolVersion(ProtocolVersion.RESP2).build());
        });
        lettuceConnectionFactory.start();
        return new ReactiveStringRedisTemplate(lettuceConnectionFactory);
    }


}
