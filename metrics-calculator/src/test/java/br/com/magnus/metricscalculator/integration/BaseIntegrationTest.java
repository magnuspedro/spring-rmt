package br.com.magnus.metricscalculator.integration;

import br.com.magnus.metricscalculator.MetricsCalculatorApplication;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;

/**
 * Base class for metrics-calculator integration tests with complete testcontainer environment.
 * All beans are real (no mocking) - tests exercise actual metrics calculation behavior.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        classes = {MetricsCalculatorApplication.class, TestRedisConfiguration.class}
)
@ActiveProfiles("integration")
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    public static final LocalStackContainer localstack = new LocalStackContainer(
            DockerImageName.parse("localstack/localstack:3.0.2"))
            .withServices(LocalStackContainer.Service.S3);

    @Container
    public static final GenericContainer<?> redis = new GenericContainer<>(
            DockerImageName.parse("redis:7.2.4-alpine"))
            .withExposedPorts(6379);

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("spring.cloud.aws.s3.endpoint", () -> localstack.getEndpointOverride(LocalStackContainer.Service.S3).toString());
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379).toString());
    }

    @BeforeEach
    void setupInfrastructure() {
        redisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();

        var s3Client = createS3Client();
        try {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket("projects")
                    .build());
        } catch (Exception e) {
            // Bucket might already exist
        }
        try {
            s3Client.createBucket(CreateBucketRequest.builder()
                    .bucket("refactored-projects")
                    .build());
        } catch (Exception e) {
            // Bucket might already exist
        }
        s3Client.close();
    }

    protected S3Client createS3Client() {
        return S3Client.builder()
                .endpointOverride(localstack.getEndpointOverride(LocalStackContainer.Service.S3))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create("fakeAccessKeyId", "fakeSecretAccessKey")))
                .region(Region.of("sa-east-1"))
                .forcePathStyle(true)
                .build();
    }
}
