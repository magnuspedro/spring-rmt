package br.com.magnus.projectsyncbff;

import br.com.magnus.projectsyncbff.configuration.QueueProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@EnableConfigurationProperties({QueueProperties.class})
public class ProjectSyncBff {

    public static void main(String[] args) {
        SpringApplication.run(ProjectSyncBff.class, args);
    }

}
