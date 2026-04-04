package br.com.magnus.config.starter.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.BucketLifecycleConfiguration;
import software.amazon.awssdk.services.s3.model.ExpirationStatus;
import software.amazon.awssdk.services.s3.model.LifecycleExpiration;
import software.amazon.awssdk.services.s3.model.LifecycleRule;
import software.amazon.awssdk.services.s3.model.LifecycleRuleFilter;
import software.amazon.awssdk.services.s3.model.PutBucketLifecycleConfigurationRequest;

import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(value = "s3.lifecycle.auto-configure", matchIfMissing = true)
public class S3LifecycleInitializer {

    private final S3Client s3Client;
    private final BucketProperties bucketProperties;

    @EventListener(ApplicationReadyEvent.class)
    public void configureBucketsLifecycle() {
        Stream.of(bucketProperties.getProjectBucket(), bucketProperties.getDownloaderBucket())
                .filter(bucket -> bucket != null && !bucket.isBlank())
                .forEach(this::applyLifecycleConfiguration);
    }

    private void applyLifecycleConfiguration(String bucket) {
        try {
            s3Client.putBucketLifecycleConfiguration(PutBucketLifecycleConfigurationRequest.builder()
                    .bucket(bucket)
                    .lifecycleConfiguration(BucketLifecycleConfiguration.builder()
                            .rules(LifecycleRule.builder()
                                    .id(bucket + "-ttl")
                                    .status(ExpirationStatus.ENABLED)
                                    .filter(LifecycleRuleFilter.builder().build())
                                    .expiration(LifecycleExpiration.builder()
                                            .days(bucketProperties.getTtlDays())
                                            .build())
                                    .build())
                            .build())
                    .build());
            log.info("Configured lifecycle for bucket {} with {} day(s)", bucket, bucketProperties.getTtlDays());
        } catch (RuntimeException exception) {
            log.warn("Failed to configure lifecycle for bucket {}", bucket, exception);
        }
    }
}
