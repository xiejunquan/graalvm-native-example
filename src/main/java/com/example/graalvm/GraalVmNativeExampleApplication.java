package com.example.graalvm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@AutoConfiguration
@SpringBootApplication
public class GraalVmNativeExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraalVmNativeExampleApplication.class, args);
	}

}
