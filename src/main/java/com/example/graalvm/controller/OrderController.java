package com.example.graalvm.controller;

import com.example.graalvm.entity.Order;
import com.example.graalvm.repository.one.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author xiejunquan
 * @create 2024/5/10 18:06
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderRepository orderRepository;

    @RequestMapping("/save")
    public Mono<Order> save(Order order){
        order.setCreateTime(System.currentTimeMillis());
        return orderRepository.save(order);
    }

    @RequestMapping("/find")
    public Mono<Order> get(Long id) {
        return orderRepository.findById(id);
    }
}
