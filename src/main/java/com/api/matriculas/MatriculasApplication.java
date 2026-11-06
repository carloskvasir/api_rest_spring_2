package com.api.matriculas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MatriculasApplication {

	public static void main(String[] args) {
		SpringApplication.run(MatriculasApplication.class, args);
	}

}
