package com.example.graalvm.controller;

import com.example.graalvm.entity.Goods;
import com.example.graalvm.repository.two.GoodsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * @author xiejunquan
 * @create 2024/7/24 9:42
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/goods")
public class GoodsController {

    private final GoodsRepository goodsRepository;

    @RequestMapping("/save")
    public Mono<Goods> save(Goods goods){
        return goodsRepository.save(goods);
    }
}
