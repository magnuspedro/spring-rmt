package br.com.messages.configuration;

import br.com.messages.repository.S3ProjectRepository;
import br.com.messages.repository.S3ProjectRepositoryImpl;
import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import io.awspring.cloud.messaging.core.QueueMessagingTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsConfiguration {

    public void property() {
        System.setProperty(SDKGlobalConfiguration.DISABLE_CERT_CHECKING_SYSTEM_PROPERTY, "true");
    }

    public AwsClientBuilder.EndpointConfiguration endpointConfiguration() {
        return new AwsClientBuilder.EndpointConfiguration("http://127.0.0.1:4566", "us-east-1");
    }

    @Bean
    public AmazonS3 amazonS3() {
        this.property();

        return AmazonS3Client.builder()
                .withEndpointConfiguration(endpointConfiguration())
                .build();
    }

    @Bean
    public S3ProjectRepository S3Repository(AmazonS3 amazonS3) {
        return new S3ProjectRepositoryImpl(amazonS3);
    }

    @Bean
    public AmazonSQSAsync amazonSQSAsync() {
        return AmazonSQSAsyncClient.asyncBuilder()
                .withEndpointConfiguration(endpointConfiguration())
                .build();
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync amazonSQSAsync) {
        return new QueueMessagingTemplate(amazonSQSAsync);
    }
}
