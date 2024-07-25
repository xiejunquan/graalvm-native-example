package com.example.graalvm.controller;

import com.example.graalvm.entity.Customer;
import com.example.graalvm.repository.one.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author xiejunquan
 * @create 2024/5/10 18:26
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerRepository customerRepository;

    @RequestMapping("/save")
    public Mono<Customer> save(Customer customer){
        return customerRepository.save(customer);
    }

    @RequestMapping("/find")
    public Mono<Customer> get(Long id) {
        return customerRepository.findById(id);
    }
}
