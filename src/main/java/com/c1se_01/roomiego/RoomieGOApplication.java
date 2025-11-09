package com.c1se_01.roomiego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class RoomieGOApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomieGOApplication.class, args);
	}

}
