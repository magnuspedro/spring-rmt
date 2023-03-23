package br.com.messages.repository;

import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Resource;
import lombok.SneakyThrows;

import java.io.InputStream;

public interface S3ProjectRepository {

    S3Resource upload(String bucket, String fileName, InputStream inputStream, ObjectMetadata objectMetadata);

    @SneakyThrows
    byte[] download(String bucket, String fileName);
}
