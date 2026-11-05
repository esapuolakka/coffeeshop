package com.websites.coffeeshop.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import software.amazon.awssdk.services.s3.S3Client;

/** With app.storage.type=s3 the application context must wire the S3 implementation. */
@SpringBootTest(properties = {
    "spring.sql.init.mode=never",
    "app.storage.type=s3",
    "app.storage.s3.bucket=demo-bucket",
    "app.storage.s3.region=eu-north-1" })
class S3StorageWiringTest {

  @Autowired
  private ImageStorage imageStorage;

  @Autowired
  private S3Client s3Client;

  @Test
  void s3ImplementationIsUsedWhenConfigured() {
    assertThat(imageStorage).isInstanceOf(S3ImageStorage.class);
    assertThat(s3Client.serviceClientConfiguration().region().id()).isEqualTo("eu-north-1");
  }
}
