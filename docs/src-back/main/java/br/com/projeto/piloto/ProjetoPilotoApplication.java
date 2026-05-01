package br.com.projeto.piloto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling; // 1. Importe isso

@SpringBootApplication
@EnableScheduling 
@ComponentScan("br.com.projeto.piloto")
@EnableJpaRepositories(basePackages = "br.com.projeto.piloto.adapter.out.jpa.repository")
public class ProjetoPilotoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProjetoPilotoApplication.class, args);
	}
}
