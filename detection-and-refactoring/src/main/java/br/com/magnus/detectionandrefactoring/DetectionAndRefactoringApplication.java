package br.com.magnus.detectionandrefactoring;

import br.com.magnus.detectionandrefactoring.configuration.QueueProperties;
import br.com.magnus.detectionandrefactoring.configuration.RefactoringProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({QueueProperties.class, RefactoringProperties.class})
public class DetectionAndRefactoringApplication {

    public static void main(String[] args) {
        SpringApplication.run(DetectionAndRefactoringApplication.class, args);
    }

}
