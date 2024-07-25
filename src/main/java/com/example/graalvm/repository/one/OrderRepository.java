package com.example.graalvm.repository.one;

import com.example.graalvm.entity.Order;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

/**
 * @author xiejunquan
 * @create 2024/5/10 17:58
 */
public interface OrderRepository extends R2dbcRepository<Order, Long> {

}
