package com.c1se_01.roomiego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.c1se_01.roomiego.model") // package chứa @Entity
@EnableJpaRepositories("com.c1se_01.roomiego.repository") // package chứa Repository
public class RoomieGOApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomieGOApplication.class, args);
	}

}
