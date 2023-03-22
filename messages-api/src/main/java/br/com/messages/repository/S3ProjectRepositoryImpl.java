package br.com.messages.repository;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class S3ProjectRepositoryImpl implements S3ProjectRepository {

    private final AmazonS3 amazonS3;

    @Override
    public PutObjectResult upload(String bucket, String fileName, InputStream inputStream, ObjectMetadata objectMetadata) {
        try {
          return  amazonS3.putObject(bucket, fileName, inputStream, objectMetadata);
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to upload the file", e);
        }
    }

    @Override
    public PutObjectResult upload(String bucket, String fileName, InputStream inputStream) {
        try {
            return amazonS3.putObject(bucket, fileName, inputStream, null);
        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to upload the file", e);
        }
    }

    @Override
    public byte[] download(String bucket, String key) {
        try {
            S3Object object = amazonS3.getObject(new GetObjectRequest(bucket, key));
            S3ObjectInputStream objectContent = object.getObjectContent();
            return IOUtils.toByteArray(objectContent);
        } catch (AmazonServiceException | IOException e) {
            throw new IllegalStateException("Failed to download the file", e);
        }
    }
}
