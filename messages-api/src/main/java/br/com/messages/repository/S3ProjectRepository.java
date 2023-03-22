package br.com.messages.repository;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;

import java.io.InputStream;

public interface S3ProjectRepository {

    PutObjectResult upload(String bucket, String fileName, InputStream inputStream, ObjectMetadata objectMetadata);

    PutObjectResult upload(String bucket, String fileName, InputStream inputStream);

    byte[] download(String bucket, String key);
}
