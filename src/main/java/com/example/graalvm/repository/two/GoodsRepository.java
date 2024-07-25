package com.example.graalvm.repository.two;

import com.example.graalvm.entity.Customer;
import com.example.graalvm.entity.Goods;
import org.springframework.data.r2dbc.repository.R2dbcRepository;

/**
 * @author xiejunquan
 * @create 2024/7/24 9:42
 */
public interface GoodsRepository extends R2dbcRepository<Goods, Long> {
}
