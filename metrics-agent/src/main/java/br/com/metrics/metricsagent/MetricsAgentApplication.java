package br.com.metrics.metricsagent;

import br.com.magnus.config.starter.configuration.RedisProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({RedisProperties.class})
public class MetricsAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetricsAgentApplication.class, args);
	}

}
