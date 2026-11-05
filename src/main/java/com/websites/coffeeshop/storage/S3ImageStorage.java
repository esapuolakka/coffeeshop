package com.websites.coffeeshop.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Stores images in a private S3 bucket. Credentials come from the default AWS
 * provider chain (the EC2 instance role when deployed).
 */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3ImageStorage implements ImageStorage {

  private final S3Client s3;
  private final String bucket;

  public S3ImageStorage(S3Client s3, @Value("${app.storage.s3.bucket}") String bucket) {
    this.s3 = s3;
    this.bucket = bucket;
  }

  @Override
  public void put(String key, byte[] content, String contentType) {
    s3.putObject(
        PutObjectRequest.builder().bucket(bucket).key(key).contentType(contentType).build(),
        RequestBody.fromBytes(content));
  }

  @Override
  public byte[] get(String key) {
    try {
      return s3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build()).asByteArray();
    } catch (NoSuchKeyException e) {
      throw new ImageNotFoundException(key, e);
    }
  }

  @Override
  public void delete(String key) {
    s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
  }
}
