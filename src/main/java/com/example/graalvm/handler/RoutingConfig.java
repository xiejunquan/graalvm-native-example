package com.example.graalvm.handler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author xiejunquan
 * @create 2024/5/10 16:13
 */
@Configuration
public class RoutingConfig {

    @Bean
    public RouterFunction<ServerResponse> helloRouter(HelloHandler helloHandler) {
        return RouterFunctions.route(
                RequestPredicates.GET("/handler/hello").and(RequestPredicates.accept(MediaType.TEXT_PLAIN)),
                helloHandler::hello);
    }

}
