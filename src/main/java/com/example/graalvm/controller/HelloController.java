package com.example.graalvm.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author xiejunquan
 * @create 2024/5/10 15:22
 */
@RestController
@RequestMapping("/controller")
public class HelloController {

    @RequestMapping("/hello")
    public Mono<String> hello(){
        return Mono.just("hello world");
    }
}
