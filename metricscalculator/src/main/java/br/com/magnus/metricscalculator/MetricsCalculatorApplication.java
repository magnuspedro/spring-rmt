package br.com.magnus.metricscalculator;

import br.com.magnus.config.starter.configuration.RedisProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({RedisProperties.class})
public class MetricsCalculatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetricsCalculatorApplication.class, args);
	}

}
