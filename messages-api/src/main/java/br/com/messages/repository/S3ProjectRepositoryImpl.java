package br.com.messages.repository;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;

@RequiredArgsConstructor
public class S3ProjectRepositoryImpl implements S3ProjectRepository {

    private final S3Template s3Template;

    @Override
    public S3Resource upload(String bucket, String fileName, InputStream inputStream, ObjectMetadata objectMetadata) {
        return s3Template.upload(bucket, fileName, inputStream, objectMetadata);
    }

    @SneakyThrows
    @Override
    public InputStream download(String bucket, String fileName) {
        S3Resource s3Resource = s3Template.download(bucket, fileName);
        return s3Resource.getInputStream();
    }
}
