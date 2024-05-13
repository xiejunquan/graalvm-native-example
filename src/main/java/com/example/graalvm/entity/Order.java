package com.example.graalvm.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

/**
 * (Order)实体类
 *
 * @author xiejunquan
 * @since 2024-05-10 17:47:33
 */
@Data
public class Order {

    @Id
    private Long id;

    private Long orderId;

    private Long customerId;

    private Long createTime;

}

