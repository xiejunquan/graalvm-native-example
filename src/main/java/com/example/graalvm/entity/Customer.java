package com.example.graalvm.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;


@Data
public class Customer {

    @Id
    private Long id;

    private String firstName;

    private String lastName;

}
