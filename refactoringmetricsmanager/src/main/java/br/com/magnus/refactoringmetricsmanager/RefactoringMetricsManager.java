package br.com.magnus.refactoringmetricsmanager;

import br.com.magnus.config.starter.configuration.BucketProperties;
import br.com.magnus.config.starter.configuration.RedisProperties;
import br.com.magnus.refactoringmetricsmanager.configuration.SqsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;
@EnableRetry
@SpringBootApplication
@EnableConfigurationProperties({BucketProperties.class, RedisProperties.class, SqsProperties.class})
public class RefactoringMetricsManager {

    public static void main(String[] args) {
        SpringApplication.run(RefactoringMetricsManager.class, args);
    }

}
