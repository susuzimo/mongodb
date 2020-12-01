package com.wtm.mondbdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication()
public class MondbdemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MondbdemoApplication.class, args);
	}



}
