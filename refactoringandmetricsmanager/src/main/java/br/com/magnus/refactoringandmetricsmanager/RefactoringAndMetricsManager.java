package br.com.magnus.refactoringandmetricsmanager;

import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.configuration.RedisProperties;
import br.com.magnus.refactoringandmetricsmanager.configuration.SqsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
@EnableRetry
@SpringBootApplication
@EnableConfigurationProperties({BucketProperties.class, RedisProperties.class, SqsProperties.class})
public class RefactoringAndMetricsManager {

    public static void main(String[] args) {
        SpringApplication.run(RefactoringAndMetricsManager.class, args);
    }

}
