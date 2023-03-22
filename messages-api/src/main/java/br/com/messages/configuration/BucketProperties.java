package br.com.messages.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "s3")
public class BucketProperties {
    private String projectBucket;
    private String refactoredProjectBucket;
}
