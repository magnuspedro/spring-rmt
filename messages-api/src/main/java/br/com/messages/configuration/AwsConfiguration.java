package br.com.messages.configuration;

import br.com.messages.repository.S3ProjectRepository;
import br.com.messages.repository.S3ProjectRepositoryImpl;
import io.awspring.cloud.s3.S3Template;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class AwsConfiguration {

    @Bean
    public SqsMessageListenerContainerFactory<Object> queueMessagingTemplate(SqsAsyncClient sqsAsyncClient) {
        return SqsMessageListenerContainerFactory.builder()
                .sqsAsyncClient(sqsAsyncClient)
                .build();
    }
    @Bean
    public S3ProjectRepository S3Repository(S3Template s3Template) {
        return new S3ProjectRepositoryImpl(s3Template);
    }
}
