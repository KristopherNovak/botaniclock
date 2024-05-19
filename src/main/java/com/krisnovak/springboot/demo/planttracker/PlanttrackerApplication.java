package com.krisnovak.springboot.demo.planttracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.krisnovak*"})
@EntityScan("com.krisnovak*")
public class PlanttrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanttrackerApplication.class, args);
	}

}
