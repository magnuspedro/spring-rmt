package br.com.magnus.projectsyncbff;

import br.com.magnus.projectsyncbff.configuration.QueueProperties;
import br.com.magnus.projectsyncbff.security.RateLimitProperties;
import br.com.magnus.projectsyncbff.validation.UploadValidationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@EnableConfigurationProperties({QueueProperties.class, UploadValidationProperties.class, RateLimitProperties.class})
public class ProjectSyncBff {

    public static void main(String[] args) {
        SpringApplication.run(ProjectSyncBff.class, args);
    }

}
