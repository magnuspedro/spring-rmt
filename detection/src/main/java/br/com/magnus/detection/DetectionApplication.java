package br.com.magnus.detection;

import br.com.magnus.config.starter.configuration.RedisProperties;
import br.com.magnus.detection.configuration.SqsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SqsProperties.class, RedisProperties.class})
public class DetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(DetectionApplication.class, args);
    }

}
