package com.Rychlewski.DesafioItauFinancia;

import org.springframework.boot.SpringApplication;

public class TestDesafioItauFinanciaApplication {

	public static void main(String[] args) {
		SpringApplication.from(DesafioItauFinanciaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
