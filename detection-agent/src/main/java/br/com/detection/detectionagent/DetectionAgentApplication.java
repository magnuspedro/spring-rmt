package br.com.detection.detectionagent;

import br.com.detection.detectionagent.configuration.SqsProperties;
import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.configuration.RedisProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SqsProperties.class, RedisProperties.class, BucketProperties.class})
public class DetectionAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(DetectionAgentApplication.class, args);
    }

}
