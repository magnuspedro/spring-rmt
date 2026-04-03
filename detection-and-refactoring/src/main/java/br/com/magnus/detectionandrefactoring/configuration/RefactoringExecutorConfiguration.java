package br.com.magnus.detectionandrefactoring.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class RefactoringExecutorConfiguration {

    @Bean(destroyMethod = "close")
    public ExecutorService detectionMethodsManagerExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("detection-methods-", 0).factory());
    }

    @Bean(destroyMethod = "close")
    public ExecutorService cinneideRefactoringExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("cinneide-refactoring-", 0).factory());
    }

    @Bean(destroyMethod = "close")
    public ExecutorService weiRefactoringExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("wei-refactoring-", 0).factory());
    }

    @Bean(destroyMethod = "close")
    public ExecutorService zafeirisRefactoringExecutor() {
        return Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("zafeiris-refactoring-", 0).factory());
    }
}
