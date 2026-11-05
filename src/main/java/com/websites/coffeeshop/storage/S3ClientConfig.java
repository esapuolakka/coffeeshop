package com.websites.coffeeshop.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

@Configuration
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3ClientConfig {

  /** Region is optional: when empty the SDK uses AWS_REGION / instance metadata. */
  @Bean(destroyMethod = "close")
  public S3Client s3Client(@Value("${app.storage.s3.region:}") String region) {
    S3ClientBuilder builder = S3Client.builder();
    if (!region.isBlank()) {
      builder.region(Region.of(region));
    }
    return builder.build();
  }
}
