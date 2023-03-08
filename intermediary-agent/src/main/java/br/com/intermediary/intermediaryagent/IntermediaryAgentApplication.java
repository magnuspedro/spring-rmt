package br.com.intermediary.intermediaryagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
public class IntermediaryAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(IntermediaryAgentApplication.class, args);
	}

}
