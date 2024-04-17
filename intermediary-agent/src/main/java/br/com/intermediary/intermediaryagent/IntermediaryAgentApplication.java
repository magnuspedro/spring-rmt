package br.com.intermediary.intermediaryagent;

import br.com.intermediary.intermediaryagent.configuration.SqsProperties;
import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.configuration.RedisProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({BucketProperties.class, RedisProperties.class, SqsProperties.class})
public class IntermediaryAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntermediaryAgentApplication.class, args);
    }

}
